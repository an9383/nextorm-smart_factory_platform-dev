package com.nextorm.portal.util;

import net.coobird.thumbnailator.Thumbnails;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageUtil {
	public static byte[] resizeBase64Image(
		@NotNull String base64Image,
		int targetWidth,
		float quality
	) {
		try {
			String[] splitStr = base64Image.split(",");
			String imageData = splitStr.length == 1
							   ? splitStr[0]
							   : splitStr[1];
			byte[] decodedImage = java.util.Base64.getDecoder()
												  .decode(imageData);
			return ImageUtil.resizeImageToWidth(decodedImage, targetWidth, quality);
		} catch (IOException e) {
			throw new RuntimeException("이미지 압축 중 오류 발생", e);
		}
	}

	public static byte[] resizeImageToWidth(
		byte[] imageData,
		int targetWidth,
		float quality
	) throws IOException {

		if (isSvg(imageData)) {
			imageData = convertSvgToPng(imageData);
		}

		try (ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
			 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			Thumbnails.of(bais)
					  .imageType(BufferedImage.TYPE_INT_ARGB) //png 이미지에 공간 데이터 포함시 ARGB 미포함시 RGB 사용됨
					  .width(targetWidth)        // 가로 사이즈만 맞춤
					  .keepAspectRatio(true)  // 비율 유지
					  .outputQuality(quality)    // 압축 품질 설정
					  .toOutputStream(baos);    // 출력

			return baos.toByteArray();
		}
	}

	private static boolean isSvg(byte[] imageData) {
		String content = new String(imageData);
		return content.contains("<svg");
	}

	private static byte[] convertSvgToPng(byte[] svgData) throws IOException {
		try (ByteArrayInputStream svgInputStream = new ByteArrayInputStream(svgData);
			 ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream()) {

			PNGTranscoder transcoder = new PNGTranscoder();
			TranscoderInput input = new TranscoderInput(svgInputStream);
			TranscoderOutput output = new TranscoderOutput(pngOutputStream);
			transcoder.transcode(input, output);

			return pngOutputStream.toByteArray();
		} catch (TranscoderException e) {
			throw new IOException("SVG 변환 중 오류 발생", e);
		}
	}

}
