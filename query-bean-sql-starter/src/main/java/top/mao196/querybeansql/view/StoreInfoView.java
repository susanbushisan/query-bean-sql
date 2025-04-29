package top.mao196.querybeansql.view;

import lombok.Data;
import top.mao196.querybeansql.annotation.ViewExposed;

/**
 * @author maoju
 * @since 2025/3/14
 **/
@ViewExposed(
        sql = "select ID, TRANSACTION_ID, REGION_CODE, AREA_L1, AREA_L2, CITY, GIS_DISTRICT, TRADE_ZONE, AGE_CLASS, STORE_CODE, STORE_NAME from EP_STORE_INFO",
        name = "ep_store_info_view",
        desc = "ep门店查询视图"
)
@Data
public class StoreInfoView {

    private Long id;

    private Long transactionId;

    private String regionCode;

    private String areaL1;

    private String areaL2;

    private String city;

    private String gisDistrict;

    private String tradeZone;

    private String ageClass;

    private String storeCode;

    private String storeName;
}
