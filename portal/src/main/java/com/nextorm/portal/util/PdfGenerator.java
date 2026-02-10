package com.nextorm.portal.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openhtmltopdf.css.parser.property.PageSize;
import com.openhtmltopdf.outputdevice.helper.BaseRendererBuilder;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.svgsupport.BatikSVGDrawer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class PdfGenerator {

	private final String PDF_FONT_PATH = "fonts/NanumGothic.ttf";
	private final String PDF_FONT_FAMILY = "Nanum Gothic";
	private final float PDF_WIDTH = PageSize.A4.getPageWidth()
											   .getFloatValue((short)0);
	private final float PDF_HEIGHT = PageSize.A4.getPageHeight()
												.getFloatValue((short)0);

	@RequiredArgsConstructor
	@Getter
	public enum HtmlTemplate {
		PARAMETER_DATA_REPORT("parameterDataReport");
		private final String templateFileName;
	}

	private final SpringTemplateEngine templateEngine;
	private final HtmlRenderer htmlRenderer;

	public byte[] generatePdfFromHtml(
		HtmlTemplate template,
		Object templateParameters
	) throws IOException {
		Context context = new Context();

		if (templateParameters != null) {
			ObjectMapper objectMapper = new ObjectMapper();
			Map<String, Object> templateParametersMap = objectMapper.convertValue(templateParameters, Map.class);
			context.setVariables(templateParametersMap);
		}

		String valueBoundHtml = templateEngine.process(template.getTemplateFileName(), context);
		String renderedHtml = htmlRenderer.getRenderedHtmlWithJavascript(valueBoundHtml);
		renderedHtml = trimSvgTextTagContent(renderedHtml);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PdfRendererBuilder builder = new PdfRendererBuilder();
		builder.withHtmlContent(renderedHtml, null);
		builder.useSVGDrawer(new BatikSVGDrawer());
		builder.toStream(baos);

		Resource resource = new ClassPathResource(PDF_FONT_PATH);
		try (InputStream fontStream = resource.getInputStream()) {
			builder.useFont(() -> fontStream, PDF_FONT_FAMILY, 400, BaseRendererBuilder.FontStyle.NORMAL, true);
		}
		builder.useDefaultPageSize(PDF_WIDTH, PDF_HEIGHT, BaseRendererBuilder.PageSizeUnits.MM);
		builder.run();

		return baos.toByteArray();
	}

	public String trimSvgTextTagContent(String html) {
		StringBuilder result = new StringBuilder();

		String regex = "(<text\\b[^>]*>)(.*?)(</text>)";
		Pattern pattern = Pattern.compile(regex, Pattern.DOTALL); // DOTALL로 줄바꿈 포함 매칭
		Matcher matcher = pattern.matcher(html);
		while (matcher.find()) {
			// 태그 안의 내용을 추출
			String content = matcher.group(2);
			// 앞뒤 공백 및 개행 문자 제거
			String trimmedContent = content.replaceAll("^[\\s\\n\\r]+|[\\s\\n\\r]+$", "");
			matcher.appendReplacement(result, matcher.group(1) + trimmedContent + matcher.group(3));
		}
		matcher.appendTail(result);
		return result.toString();
	}
}
