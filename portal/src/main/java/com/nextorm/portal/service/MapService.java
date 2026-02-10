package com.nextorm.portal.service;

import com.nextorm.common.db.repository.ReservoirLastRepository;
import com.nextorm.portal.dto.InfraBylocationReponseDto;
import com.nextorm.portal.dto.ReservoirLastResponseDto;
import com.nextorm.portal.restapi.KakaoMapRestApi;
import com.nextorm.portal.restapi.OverpassRestApi;
import com.nextorm.portal.restapi.dto.kakaomap.KakaoMapRequestDto;
import com.nextorm.portal.restapi.dto.kakaomap.KakaoQuerySearchDto;
import com.nextorm.portal.restapi.dto.openstreetmap.OverpassSearchDto;
import com.nextorm.portal.util.GeoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class MapService {
	private final ReservoirLastRepository reservoirLastRepository;
	private final KakaoMapRestApi kakaoMapRestApi;
	private final OverpassRestApi overpassRestApi;

	public List<ReservoirLastResponseDto> getMaps() {
		return reservoirLastRepository.findAll()
									  .stream()
									  .map(ReservoirLastResponseDto::from)
									  .toList();
	}

	public List<InfraBylocationReponseDto> getInfraByLocation(
		double lat,
		double lng,
		long radius
	) {
		// 조회반경 20km 제한
		if (radius > 20000) {
			radius = 20000;
		}

		// Overpass API 호출 및 처리
		List<InfraBylocationReponseDto> overpassInfraDatas = getOverpassInfraData(lat, lng, radius);

		// Kakaomap API 호출 및 처리
		List<InfraBylocationReponseDto> kakaoMapInfraDatas = getKakaoMapInfraData(lat, lng, radius);

		// Overpass 결과에서 kakaomap 좌표와 100m 거리 겹치는 데이터
		List<InfraBylocationReponseDto> filteredOverpassDataByDistance = filteredOverpassOverlapData(overpassInfraDatas,
			kakaoMapInfraDatas);

		//Overpass 결과값에서 필터링 된 좌표값 제거 및 카카오맵 데이터 추가
		List<InfraBylocationReponseDto> resultInfraDatas = new ArrayList<>();
		resultInfraDatas.addAll(filteredOverpassDataByDistance);
		resultInfraDatas.addAll(kakaoMapInfraDatas);

		return resultInfraDatas;

	}

	/**
	 * overpass API 데이터중 kakao 데이터와 100m 이내로 겹치는 부분 제거
	 *
	 * @param overpassInfraDatas
	 * @param kakaoMapInfraDatas
	 * @return
	 */
	private List<InfraBylocationReponseDto> filteredOverpassOverlapData(
		List<InfraBylocationReponseDto> overpassInfraDatas,
		List<InfraBylocationReponseDto> kakaoMapInfraDatas
	) {
		int distance = 100;
		final Predicate<InfraBylocationReponseDto> filteredDistancePerMiter =

			resInfraData -> kakaoMapInfraDatas.stream()
											  .noneMatch(listElement -> GeoUtil.calculateDistanceByHaversine(
												  resInfraData.getLat(),
												  resInfraData.getLon(),
												  listElement.getLat(),
												  listElement.getLon()) <= distance);
		// 필터링된 결과 생성
		return overpassInfraDatas.stream()
								 .filter(filteredDistancePerMiter) // 100m 내의 좌표 겹치면 카카오 데이터로 치환
								 .toList();
	}

	private List<InfraBylocationReponseDto> getKakaoMapInfraData(
		double lat,
		double lng,
		long radius
	) {
		String sort = "distance";
		List<KakaoQuerySearchDto.Documents> kakaoResponseDocuments = new ArrayList<>();
		String[] queries = {"음식점", "공장"};

		for (String query : queries) {
			long page = 1;
			while (true) {
				KakaoMapRequestDto requestDto = KakaoMapRequestDto.builder()
																  .query(query)
																  .latitude(lat)
																  .longitude(lng)
																  .radius(radius)
																  .page(page)
																  .sort(sort)
																  .size(15L)
																  .build();

				KakaoQuerySearchDto currentResponse = kakaoMapRestApi.getKakaoMapInfraData(requestDto);

				if (currentResponse == null) {
					return Collections.emptyList();
				}

				boolean isEnd = currentResponse.getMeta()
											   .isEnd();
				kakaoResponseDocuments.addAll(List.of(currentResponse.getDocuments()));
				if (isEnd) {
					break;
				}

				page++;
			}
		}
		return kakaoResponseDocuments.stream()
									 .map(document -> {
										 InfraBylocationReponseDto infraData = new InfraBylocationReponseDto();

										 infraData.setLat(Double.parseDouble(document.getY()));
										 infraData.setLon(Double.parseDouble(document.getX()));
										 infraData.setName(document.getPlaceName());
										 if (document.getCategoryName()
													 .contains("카페")) {
											 infraData.setType(InfraBylocationReponseDto.Type.CAFE);
										 } else if (document.getCategoryName()
															.contains("음식점")) {
											 infraData.setType(InfraBylocationReponseDto.Type.RESTAURANT);
										 } else {
											 infraData.setType(InfraBylocationReponseDto.Type.INDUSTRIAL);
										 }
										 return infraData;
									 })
									 .toList();

	}

	private List<InfraBylocationReponseDto> getOverpassInfraData(
		double lat,
		double lng,
		long radius
	) {
		// 공장 인것 필터링
		final Predicate<OverpassSearchDto.Element> isFactory = element -> "way".equals(element.getType()) && "industrial".equals(
			element.getTags()
				   .getLanduse());

		OverpassSearchDto overpassResponse = overpassRestApi.getOverpassInfraData(lat, lng, radius);
		if (overpassResponse.getElements() == null) {
			return Collections.emptyList();
		}
		overpassResponse.getElements()
						.stream()
						.filter(isFactory)
						.forEach(element -> {

							List<OverpassSearchDto.Geometry> geometry = element.getGeometry();

							if (geometry != null) {

								double avgLat = geometry.stream()
														.mapToDouble(OverpassSearchDto.Geometry::getLat)
														.average()
														.orElse(0);

								double avgLon = geometry.stream()
														.mapToDouble(OverpassSearchDto.Geometry::getLon)
														.average()
														.orElse(0);
								element.setLat(avgLat);
								element.setLon(avgLon);
							}
						});

		List<InfraBylocationReponseDto> overpassInfra = new ArrayList<>();

		overpassResponse.getElements()
						.forEach(element -> {
							InfraBylocationReponseDto resultInfraData = new InfraBylocationReponseDto();
							resultInfraData.setName(element.getTags()
														   .getName());
							resultInfraData.setLat(element.getLat());
							resultInfraData.setLon(element.getLon());

							InfraBylocationReponseDto.Type type;
							if (element.getTags()
									   .getLanduse() != null) {
								type = InfraBylocationReponseDto.Type.INDUSTRIAL;
							} else {
								if (element.getTags()
										   .getAmenity()
										   .equals("cafe")) {
									type = InfraBylocationReponseDto.Type.CAFE;
								} else {
									type = InfraBylocationReponseDto.Type.RESTAURANT;
								}
							}
							resultInfraData.setType(type);
							overpassInfra.add(resultInfraData);
						});

		return overpassInfra;
	}
}
