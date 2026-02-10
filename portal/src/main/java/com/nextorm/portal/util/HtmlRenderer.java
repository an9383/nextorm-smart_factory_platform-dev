package com.nextorm.portal.util;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HtmlRenderer {

	private final int DEFAULT_JAVASCRIPT_TIMEOUT_MS = 3 * 1000;

	public String getRenderedHtmlWithJavascript(
		String sourceHtml
	) throws IOException {
		return this.getRenderedHtmlWithJavascript(sourceHtml, DEFAULT_JAVASCRIPT_TIMEOUT_MS);
	}

	public String getRenderedHtmlWithJavascript(
		String sourceHtml,
		long javascriptTimeoutMs
	) throws IOException {
		try (WebClient webClient = new WebClient()) {
			webClient.getOptions()
					 .setJavaScriptEnabled(true);
			webClient.getOptions()
					 .setCssEnabled(true);
			HtmlPage page = webClient.loadHtmlCodeIntoCurrentWindow(sourceHtml);

			//Javascript 타임아웃 시간
			webClient.waitForBackgroundJavaScript(javascriptTimeoutMs);

			return page.asXml();
		}
	}
}
