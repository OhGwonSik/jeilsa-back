package com.jeil.delivery.configuration.mail.domain;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MailHistoryVO {

	private Integer mailHistoryId;
	private Integer toCompanyId;
	private Integer fromCompanyId;
	private String mailStatusCd;
	private String failLog;
	private LocalDateTime sendDt;
	private Integer billId;
	private Integer billDtlId;

    private Integer regId;
    private LocalDateTime regDt;
    private Integer chgId;
    private LocalDateTime chgDt;
    private String delYn;

}
