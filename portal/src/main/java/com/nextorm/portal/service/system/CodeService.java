package com.nextorm.portal.service.system;

import com.nextorm.common.db.entity.system.code.Code;
import com.nextorm.common.db.entity.system.code.CodeCategory;
import com.nextorm.common.db.repository.system.code.CodeCategoryRepository;
import com.nextorm.common.db.repository.system.code.CodeRepository;
import com.nextorm.portal.common.exception.code.CodeDuplicationException;
import com.nextorm.portal.common.exception.code.CodeNotFoundException;
import com.nextorm.portal.common.exception.codecategory.CodeCategoryDuplicationException;
import com.nextorm.portal.common.exception.codecategory.CodeCategoryNotFoundException;
import com.nextorm.portal.common.exception.codecategory.RelateCodeCategoryNotFoundException;
import com.nextorm.portal.dto.base.BaseTreeItem;
import com.nextorm.portal.dto.system.*;
import com.nextorm.portal.util.TreeUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class CodeService {

	private final CodeRepository codeRepository;
	private final CodeCategoryRepository codeCategoryRepository;

	public List<BaseTreeItem> getCodeTree() {

		List<CodeCategory> categories = codeCategoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
		List<Code> codes = codeRepository.findAllWithCategory(Sort.by(Sort.Direction.ASC, "sort"));

		List<BaseTreeItem> treeItems = categories.stream()
												 .map(category -> new BaseTreeItem.Builder(category.getId(),
													 category.getName(),
													 null).type("CATEGORY")
														  .parentType(null)
														  .object(CodeCategoryResponseDto.from(category))
														  .build())
												 .collect(Collectors.toList());

		List<BaseTreeItem> codeItems = codes.stream()
											.map(code -> new BaseTreeItem.Builder(code.getId(),
												code.getName(),
												code.getCategory()
													.getId()).type("CODE")
															 .parentType("CATEGORY")
															 .object(CodeListResponseDto.from(code))
															 .build())
											.toList();
		treeItems.addAll(codeItems);

		return TreeUtil.generateTreeHierarchy(treeItems);
	}

	public CodeResponseDto getCode(Long id) {
		Code code = codeRepository.findByIdWithCategory(id)
								  .orElseThrow(CodeNotFoundException::new);
		return CodeResponseDto.from(code);
	}

	public CodeCategoryResponseDto getCodeCategory(Long id) {
		CodeCategory codeCategory = codeCategoryRepository.findById(id)
														  .orElseThrow(CodeCategoryNotFoundException::new);
		return CodeCategoryResponseDto.from(codeCategory);
	}

	public List<CodeListResponseDto> getCodesByCategory(String category) {
		List<Code> codes = codeRepository.findByCategory(category);
		return CodeListResponseDto.from(codes);
	}

	public CodeCategoryResponseDto createCategory(CodeCategoryCreateRequestDto categoryCreateRequest) {

		codeCategoryRepository.findByCategory(categoryCreateRequest.getCategory())
							  .ifPresent(codeCategory -> {
								  throw new CodeCategoryDuplicationException(categoryCreateRequest.getCategory());
							  });

		CodeCategory codeCategory = categoryCreateRequest.toEntity();
		codeCategoryRepository.save(codeCategory);

		return CodeCategoryResponseDto.from(codeCategory);
	}

	@Transactional
	public CodeCategoryResponseDto updateCategory(
		Long id,
		CodeCategoryUpdateRequestDto categoryUpdateRequest
	) {
		CodeCategory codeCategory = codeCategoryRepository.findById(id)
														  .orElseThrow(CodeCategoryNotFoundException::new);
		codeCategory.modify(categoryUpdateRequest.getName(), categoryUpdateRequest.getDescription());
		return CodeCategoryResponseDto.from(codeCategory);
	}

	@Transactional
	public CodeCategoryResponseDto deleteCategory(Long id) {
		CodeCategory codeCategory = codeCategoryRepository.findById(id)
														  .orElseThrow(CodeCategoryNotFoundException::new);

		//하위의 모든 코드 삭제
		List<Code> codes = codeRepository.findByCategory(codeCategory.getCategory());
		codeRepository.deleteAll(codes);

		//코드 카테고리 삭제
		codeCategoryRepository.delete(codeCategory);

		return CodeCategoryResponseDto.from(codeCategory);
	}

	@Transactional
	public List<CodeListResponseDto> sortCodes(
		Long categoryId,
		List<Long> codeIds
	) {
		List<Code> codes = codeRepository.findByCategoryIdAndIds(categoryId, codeIds);

		List<Code> sortedCodes = codes.stream()
									  .sorted((c1, c2) -> {
										  int index1 = codeIds.indexOf(c1.getId());
										  int index2 = codeIds.indexOf(c2.getId());
										  return Integer.compare(index1, index2);
									  })
									  .toList();
		IntStream.range(0, sortedCodes.size())
				 .forEach(index -> {
					 sortedCodes.get(index)
								.modifySort(index + 1);
				 });

		return CodeListResponseDto.from(sortedCodes);
	}

	@Transactional
	public CodeResponseDto createCode(CodeCreateRequestDto codeCreateRequest) {

		CodeCategory category = codeCategoryRepository.findById(codeCreateRequest.getCategoryId())
													  .orElseThrow(RelateCodeCategoryNotFoundException::new);

		List<Code> categoryCodes = codeRepository.findByCategory(category.getCategory());

		//카테고리 내에 중복된 코드가 있는지 확인
		categoryCodes.stream()
					 .filter(code -> code.getCode()
										 .equals(codeCreateRequest.getCode()))
					 .findAny()
					 .ifPresent(code -> {
						 throw new CodeDuplicationException(codeCreateRequest.getCode());
					 });

		CodeCategory childCategory = codeCreateRequest.getChildCategoryId() != null
									 ? codeCategoryRepository.findById(codeCreateRequest.getChildCategoryId())
															 .orElseThrow(RelateCodeCategoryNotFoundException::new)
									 : null;

		List<Code> childCodes = codeRepository.findAllById(codeCreateRequest.getChildCodeIds());

		int sort = categoryCodes.size() + 1; //정렬 순서는 등록된 수량 + 1
		Code code = codeCreateRequest.toEntity(category, childCategory, childCodes, sort);
		codeRepository.save(code);

		return CodeResponseDto.from(code);
	}

	@Transactional
	public CodeResponseDto updateCode(
		Long id,
		CodeUpdateRequestDto codeUpdateRequest
	) {
		Code code = codeRepository.findById(id)
								  .orElseThrow(CodeNotFoundException::new);

		CodeCategory childCategory = codeUpdateRequest.getChildCategoryId() != null
									 ? codeCategoryRepository.findById(codeUpdateRequest.getChildCategoryId())
															 .orElseThrow(RelateCodeCategoryNotFoundException::new)
									 : null;

		List<Code> childCodes = codeRepository.findAllById(codeUpdateRequest.getChildCodeIds());

		code.modify(codeUpdateRequest.getName(),
			codeUpdateRequest.getValue(),
			codeUpdateRequest.getDescription(),
			childCategory,
			childCodes);

		return CodeResponseDto.from(code);
	}

	@Transactional
	public CodeListResponseDto deleteCode(Long id) {
		Code code = codeRepository.findById(id)
								  .orElseThrow(CodeNotFoundException::new);
		codeRepository.delete(code);
		return CodeListResponseDto.from(code);
	}

	@Transactional
	public List<CodeListResponseDto> deleteCodes(List<Long> ids) {

		List<Code> codes = codeRepository.findAllById(ids);
		codeRepository.deleteAll(codes);
		return codes.stream()
					.map(CodeListResponseDto::from)
					.toList();
	}
}
