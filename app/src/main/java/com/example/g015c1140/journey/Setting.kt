package com.example.g015c1140.journey

class Setting {

//    val SERVER_ADDRESS = "35.200.26.70"
    val SERVER_ADDRESS = "api.mino.asia"
//    val IP_ADDRESS = "192.168.43.97:3000"
    val IP_ADDRESS = "$SERVER_ADDRESS:3001"


    val SERVER_IMAGE_POST_URL = "http://$IP_ADDRESS/api/v1/image/upload"
    val SERVER_IMAGE_DELETE_URL = "http://$IP_ADDRESS/api/v1/image/delete?"
    val USER_IMAGE_GET_URL = "http://$SERVER_ADDRESS:8080/test1/"

    val USER_LOGIN_URL = "http://$IP_ADDRESS/api/v1/users/login"
    val USER_GET_URL = "http://$IP_ADDRESS/api/v1/users/find"
    val USER_ACCOUNT_GET_URL = "http://$IP_ADDRESS/api/v1/users/find?user_id="
    val USER_POST_URL = "http://$IP_ADDRESS/api/v1/users/register"
    val USER_PUT_URL = "http://$IP_ADDRESS/api/v1/users/update"
    val IMAGE_POST_URL = "http://$IP_ADDRESS/api/v1/image/upload"
    val IMAGE_DELETE_URL = "http://$IP_ADDRESS/api/v1/image/delete?"
    val SPOT_POST_URL = "http://$IP_ADDRESS/api/v1/spot/register/"
    val SPOT_DELETE_URL = "http://$IP_ADDRESS/api/v1/spot/delete?"
    val SPOT_GET_PID_URL = "http://$IP_ADDRESS/api/v1/spot/find?plan_id="
    val SPOT_GET_SID_URL = "http://$IP_ADDRESS/api/v1/spot/find?spot_id="
    val PLAN_POST_URL = "http://$IP_ADDRESS/api/v1/plan/register/"
    val PLAN_DELETE_URL = "http://$IP_ADDRESS/api/v1/plan/delete?"
    val PLAN_GET_PID_URL = "http://$IP_ADDRESS/api/v1/plan/find?plan_id="
    val PLAN_GET_UID_URL = "http://$IP_ADDRESS/api/v1/plan/find?user_id="
    val TIMELINE_GET_URL = "http://$IP_ADDRESS/api/v1/timeline/find?offset="
    val FAVORITE_GET_COUNT_URL = "http://$IP_ADDRESS/api/v1/favorite/count?plan_id="
    val FAVORITE_GET_UID_URL = "http://$IP_ADDRESS/api/v1/favorite/find?user_id="
    val FAVORITE_GET_PUID_URL = "http://$IP_ADDRESS/api/v1/favorite/find?plan_id=%s&user_id=%s"
    val FAVORITE_POST_URL = " http://$IP_ADDRESS/api/v1/favorite/register"
    val FAVORITE_DELETE_URL = "http://$IP_ADDRESS/api/v1/favorite/delete?"
    val SEARCH_GET_URL = "http://$IP_ADDRESS/api/v1/search/find?"

    val USER_SHARED_PREF = "UserData"
    val USER_SHARED_PREF_FLG = "userFlg"
    val USER_SHARED_PREF_ICONIMAGE = "iconImage"
    val USER_SHARED_PREF_ID = "id"
    val USER_SHARED_PREF_NAME = "name"
    val USER_SHARED_PREF_PASSWORD = "password"
    val USER_SHARED_PREF_GENERATION = "generation"
    val USER_SHARED_PREF_GENDER = "gender"
    val USER_SHARED_PREF_TOKEN = "token"
    val USER_SHARED_PREF_COMMENT = "comment"
    val USER_SHARED_PREF_HEADERIMAGE = "headerImage"
}