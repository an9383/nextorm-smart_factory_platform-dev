package com.nextorm.portal.service;

import com.nextorm.common.db.entity.Location;
import com.nextorm.common.db.entity.Tool;
import com.nextorm.common.db.repository.LocationRepository;
import com.nextorm.common.db.repository.ToolRepository;
import com.nextorm.portal.common.exception.ConstraintViloationException;
import com.nextorm.portal.common.exception.location.LocationDuplicateException;
import com.nextorm.portal.common.exception.location.LocationNotFoundException;
import com.nextorm.portal.common.exception.location.ParentLocationNotFoundException;
import com.nextorm.portal.dto.base.BaseTreeItem;
import com.nextorm.portal.dto.common.ConstraintViloationDto;
import com.nextorm.portal.dto.location.LocationCreateRequestDto;
import com.nextorm.portal.dto.location.LocationCreateResponseDto;
import com.nextorm.portal.dto.location.LocationDto;
import com.nextorm.portal.dto.location.LocationModifyRequestDto;
import com.nextorm.portal.dto.tool.ToolResponseDto;
import com.nextorm.portal.util.TreeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LocationService {
	private static final Sort SORT_BY_CREATE_AT_ASC = Sort.by(Sort.Direction.ASC, "createAt");

	private final LocationRepository locationRepository;
	private final ToolRepository toolRepository;

	public List<BaseTreeItem> getLocationsTree(Location.Type... typeUntils) {
		List<LocationDto> locationDtos = this.getLocations(typeUntils);

		List<BaseTreeItem> itemList = locationDtos.stream()
												  .map(item -> new BaseTreeItem.Builder(item.getId(),
													  item.getName(),
													  item.getParent() == null
													  ? null
													  : item.getParent()
															.getId()).type(item.getType()
																			   .name())
																	 .parentType(item.getType()
																					 .getParent() != null
																				 ? item.getType()
																					   .getParent()
																					   .name()
																				 : null)
																	 .object(item)
																	 .build())
												  .toList();
		return TreeUtil.generateTreeHierarchy(itemList);
	}

	public List<LocationDto> getLocationChildren(long id) {
		Location parent = Location.builder()
								  .id(id)
								  .build();

		return locationRepository.findByParent(parent, Sort.by(Sort.Direction.ASC, "name"))
								 .stream()
								 .map(LocationDto::of)
								 .toList();
	}

	public List<LocationDto> getLocations(Location.Type... typeUntils) {
		List<LocationDto> locationDtos = this.getLocationsWithParent();
		if (typeUntils != null && typeUntils.length > 0) {
			Set<Location.Type> types = new HashSet<>();
			for (Location.Type typeUntil : typeUntils) {
				types.add(typeUntil);
				types.addAll(Location.Type.getAncestors(typeUntil));
			}
			locationDtos = locationDtos.stream()
									   .filter(locationDto -> types.contains(locationDto.getType()))
									   .toList();
		}
		return locationDtos;
	}

	private List<LocationDto> getLocationsWithParent() {
		List<Location> locations = locationRepository.findAllWithParent(SORT_BY_CREATE_AT_ASC);
		return locations.stream()
						.map(LocationDto::of)
						.toList();
	}

	public LocationDto getLocation(long locationId) {
		return locationRepository.findById(locationId)
								 .map(LocationDto::of)
								 .orElse(null);
	}

	public LocationCreateResponseDto create(LocationCreateRequestDto createRequestDto) {
		Optional<Location> duplicateLocation = locationRepository.findByNameAndTypeAndParentId(createRequestDto.getName(),
			createRequestDto.getType(),
			createRequestDto.getParentId());
		if (duplicateLocation.isPresent()) {
			throw new LocationDuplicateException(createRequestDto.getName());
		}

		Location parent = null;
		if (createRequestDto.getType() != Location.Type.SITE) {
			parent = locationRepository.findById(createRequestDto.getParentId())
									   .orElseThrow(ParentLocationNotFoundException::new);
		}

		Location location = Location.create(createRequestDto.toEntity(), parent);
		locationRepository.save(location);
		return LocationCreateResponseDto.of(location, parent);
	}

	public LocationDto modify(
		Long id,
		LocationModifyRequestDto modifyRequestDto
	) {
		Location location = locationRepository.findById(id)
											  .orElseThrow(LocationNotFoundException::new);

		boolean isNameChanged = !location.getName()
										 .equals(modifyRequestDto.getName());

		if (isNameChanged) {
			Long parentId = location.getParent()
									.getId();
			locationRepository.findByNameAndParentId(modifyRequestDto.getName(), parentId)
							  .ifPresent(duplicate -> {
								  throw new LocationDuplicateException(modifyRequestDto.getName());
							  });
		}
		location.modify(modifyRequestDto.toEntity());
		return LocationDto.of(location);
	}

	public void delete(Long id) {
		List<ConstraintViloationDto> exists = new ArrayList<>();
		Optional<Location> existLocation = locationRepository.findById(id);

		if (existLocation.isEmpty()) {
			throw new LocationNotFoundException();
		}

		List<ConstraintViloationDto> curLocation = this.involvedOtherTableData(id);
		if (!curLocation.isEmpty()) {
			exists.addAll(curLocation);
		}

		List<LocationDto> childrenDto = this.getLocationChildren(id);
		if (!childrenDto.isEmpty()) {
			for (LocationDto childDto : childrenDto) {
				try {
					this.delete(childDto.getId());
				} catch (ConstraintViloationException e) {
					exists.addAll(e.getData());
				}
			}
		}
		if (!exists.isEmpty()) {
			throw new ConstraintViloationException(exists);
		}
		locationRepository.deleteById(id);
	}

	public void delete(List<Long> ids) throws ConstraintViloationException {
		List<ConstraintViloationDto> constraintDatas = new ArrayList<>();
		for (Long id : ids) {
			try {
				this.delete(id);
			} catch (ConstraintViloationException e) {
				constraintDatas.addAll(e.getData());
			}
		}
		if (!constraintDatas.isEmpty()) {
			throw new ConstraintViloationException(constraintDatas);
		}
	}

	private List<ConstraintViloationDto> involvedOtherTableData(Long id) {
		List<ConstraintViloationDto> exists = new ArrayList<>();
		List<Tool> tools = toolRepository.findByLocationId(id, SORT_BY_CREATE_AT_ASC);
		if (!tools.isEmpty()) {
			exists.addAll(tools.stream()
							   .map(tool -> new ConstraintViloationDto("Modeling > Tool > " + tool.getLocation()
																								  .getParent()
																								  .getName() + " > " + tool.getLocation()
																														   .getName(),
								   tool.getId(),
								   tool.getName(),
								   null,
								   null))
							   .toList());
		}
		return exists;
	}

	public List<BaseTreeItem> getLocationsAndToolsTree() {
		List<LocationDto> locationsDto = getLocations(Location.Type.LINE);
		List<BaseTreeItem> itemList = locationsDto.stream()
												  .map(item -> new BaseTreeItem.Builder(item.getId(),
													  item.getName(),
													  item.getParent() == null
													  ? null
													  : item.getParent()
															.getId()).type(item.getType()
																			   .name())
																	 .parentType(item.getType()
																					 .getParent() != null
																				 ? item.getType()
																					   .getParent()
																					   .name()
																				 : null)
																	 .build())
												  .collect(Collectors.toList());

		List<ToolResponseDto> toolDtos = toolRepository.findAllWithLocation(Sort.by(Sort.Direction.ASC, "createAt"))
													   .stream()
													   .map(ToolResponseDto::from)
													   .toList();

		List<BaseTreeItem> toolItems = toolDtos.stream()
											   .map(toolDto -> new BaseTreeItem.Builder(toolDto.getId(),
												   toolDto.getName(),
												   toolDto.getLocation()
														  .getId()).type("TOOL")
																   .object(toolDto)
																   .parentType(toolDto.getLocation()
																					  .getType()
																					  .name())
																   .build())
											   .toList();

		itemList.addAll(toolItems);
		return TreeUtil.generateTreeHierarchy(itemList);
	}

	public List<ToolResponseDto> getLocationUnderTools(Long locationId) {
		return locationRepository.findById(locationId)
								 .map(location -> {
									 List<Long> lineIds = locationRepository.findLocationUnderLineIds(locationId);
									 return toolRepository.findByLocationIdIn(lineIds,
															  Sort.by(Sort.Direction.ASC, "name"))
														  .stream()
														  .map(ToolResponseDto::from)
														  .toList();
								 })
								 .orElse(List.of());
	}

	public LocationDto getLineTypeLocationByToolId(Long toolId) {
		Location lineTypeLocation = locationRepository.findByToolIdAndType(toolId);
		return LocationDto.of(lineTypeLocation);
	}
}
