package com.nextorm.portal.entity.system;

import com.nextorm.common.db.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "system_info")
@SuperBuilder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SystemInfo extends BaseEntity {
	// key는 예약어라 사용이 불가능하여 system_key를 사용한다
	@Column(name = "system_key", unique = true)
	private String key;

	@Column(name = "value", length = 1000)
	private String value;

	@Comment("대용량 값 (ex. 이미지)")
	@Lob
	@Column(name = "large_value", columnDefinition = "MEDIUMBLOB")    // 16MB
	private byte[] largeValue;

	public String getLargeValue() {
		if (largeValue == null) {
			return null;
		}
		return "data:image/jpeg;base64," + java.util.Base64.getEncoder()
														   .withoutPadding()
														   .encodeToString(largeValue);
	}

	public void modify(
		String value,
		byte[] resizedImage
	) {
		this.value = value;
		this.largeValue = resizedImage;
	}
}
