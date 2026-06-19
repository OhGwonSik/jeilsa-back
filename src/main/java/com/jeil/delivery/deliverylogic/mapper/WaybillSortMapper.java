package com.jeil.delivery.deliverylogic.mapper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class WaybillSortMapper {
    private static final Map<String, String> ALLOWED;

    static {
        Map<String, String> tmp = new HashMap<>();
        tmp.put("waybillId", "b.waybill_id");
        tmp.put("senderCompanyNm", "b.sender_company_nm");
        tmp.put("receiverCompanyNm", "b.receiver_company_nm");
        tmp.put("qty", "b.qty");
        ALLOWED = Collections.unmodifiableMap(tmp);
    }

    public static String toOrderBy(String sortCol, String sortDir) {
        String col = ALLOWED.get(sortCol);
        if (col == null || col.isBlank()) col = "b.waybill_id";
        String dir = "DESC";
        if ("asc".equalsIgnoreCase(sortDir)) dir = "ASC";
        else if ("desc".equalsIgnoreCase(sortDir)) dir = "DESC";
        return col + " " + dir;
    }
    private WaybillSortMapper() {}
}