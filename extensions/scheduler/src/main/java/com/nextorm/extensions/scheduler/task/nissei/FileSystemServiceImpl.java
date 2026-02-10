package com.nextorm.extensions.scheduler.task.nissei;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;
import java.util.Optional;

/**
 * 파일 시스템 작업의 실제 구현체
 */
@Slf4j
public class FileSystemServiceImpl implements FileSystemService {

	@Override
	public Optional<String> findLatestCsvFile(String directoryPath) {
		try {
			Path directory = Paths.get(directoryPath);

			if (!Files.exists(directory) || !Files.isDirectory(directory)) {
				log.warn("CSV 디렉토리가 존재하지 않거나 디렉토리가 아닙니다: {}", directoryPath);
				return Optional.empty();
			}

			try (var stream = Files.list(directory)) {
				Optional<Path> latestFile = stream.filter(path -> path.toString()
																	  .toLowerCase()
																	  .endsWith(".csv"))
												  .filter(Files::isRegularFile)
												  .max(Comparator.comparing(path -> {
													  try {
														  return Files.getLastModifiedTime(path);
													  } catch (IOException e) {
														  log.error("파일 수정 시간 조회 실패: {}", path, e);
														  return FileTime.fromMillis(0);
													  }
												  }));

				if (latestFile.isPresent()) {
					String fileName = latestFile.get()
												.getFileName()
												.toString();
					log.debug("가장 최근 CSV 파일 발견: {}", fileName);
					return Optional.of(fileName);
				} else {
					log.warn("CSV 파일을 찾을 수 없습니다: {}", directoryPath);
					return Optional.empty();
				}
			}
		} catch (IOException e) {
			log.error("CSV 파일 검색 중 오류 발생: {}", directoryPath, e);
			return Optional.empty();
		}
	}
}
