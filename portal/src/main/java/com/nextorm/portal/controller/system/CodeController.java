package com.nextorm.portal.controller.system;

import com.nextorm.portal.dto.base.BaseTreeItem;
import com.nextorm.portal.dto.system.*;
import com.nextorm.portal.service.system.CodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/codes")
@RequiredArgsConstructor
public class CodeController {

	private final CodeService codeService;

	@GetMapping("/tree")
	public List<BaseTreeItem> getCodeTree() {
		return codeService.getCodeTree();
	}

	@GetMapping("/{id}")
	public CodeResponseDto getCode(@PathVariable("id") Long id) {
		return codeService.getCode(id);
	}

	@GetMapping("/category/{id}")
	public CodeCategoryResponseDto getCodeCategory(@PathVariable("id") Long id) {
		return codeService.getCodeCategory(id);
	}

	@GetMapping
	public List<CodeListResponseDto> getCodesByCategory(@RequestParam("category") String category) {
		return codeService.getCodesByCategory(category);
	}

	@PostMapping("/category")
	public CodeCategoryResponseDto createCategory(@RequestBody CodeCategoryCreateRequestDto categoryCreateRequest) {
		return codeService.createCategory(categoryCreateRequest);
	}

	@PutMapping("/category/{id}")
	public CodeCategoryResponseDto modifyCategory(
		@PathVariable("id") Long id,
		@RequestBody CodeCategoryUpdateRequestDto categoryUpdateRequest
	) {
		return codeService.updateCategory(id, categoryUpdateRequest);
	}

	@DeleteMapping("/category/{id}")
	public CodeCategoryResponseDto deleteCategory(
		@PathVariable("id") Long id
	) {
		return codeService.deleteCategory(id);
	}

	@PutMapping("/sort")
	public List<CodeListResponseDto> sortCodes(@RequestBody CodeSortRequestDto codeSortRequest) {
		return codeService.sortCodes(codeSortRequest.getCategoryId(), codeSortRequest.getCodeIds());
	}

	@PostMapping
	public CodeResponseDto createCode(@RequestBody CodeCreateRequestDto codeCreateRequest) {
		return codeService.createCode(codeCreateRequest);
	}

	@PutMapping("/{id}")
	public CodeResponseDto modifyCode(
		@PathVariable("id") Long id,
		@RequestBody CodeUpdateRequestDto codeUpdateRequest
	) {
		return codeService.updateCode(id, codeUpdateRequest);
	}

	@DeleteMapping("/{id}")
	public CodeListResponseDto deleteCode(
		@PathVariable("id") Long id
	) {
		return codeService.deleteCode(id);
	}

	@PostMapping("/bulk-delete")
	public List<CodeListResponseDto> deleteCodes(
		@RequestBody List<Long> ids
	) {
		return codeService.deleteCodes(ids);
	}

}
