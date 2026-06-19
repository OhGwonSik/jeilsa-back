package com.jeil.delivery.configuration.mail.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class MailDTO {

	private String from; // 발신자
	private String to; // 수신자
	private String title; // 제목

}
