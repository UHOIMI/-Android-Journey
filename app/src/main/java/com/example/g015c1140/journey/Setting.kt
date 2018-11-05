package com.example.g015c1140.journey

class Setting {

    val IP_ADDRESS = ""
    val SERVER_ADDRESS = ""

    val USER_LOGIN_URL = "http://$IP_ADDRESS/api/v1/users/login"
    val USER_GET_URL = "http://$IP_ADDRESS/api/v1/users/find"
    val USER_ACCOUNT_GET_URL = "http://$IP_ADDRESS/api/v1/users/find?user_id="
    val USER_POST_URL = "http://$IP_ADDRESS/api/v1/users/register"
    val SPOT_IMAGE_POST_URL = "http://$IP_ADDRESS/api/v1/image/upload"
    val SPOT_POST_URL = "http://$IP_ADDRESS/api/v1/spot/register/"
    //    val SPOT_GET_URL = "http://$IP_ADDRESS/api/v1/spot/find?user_id=$USER_ID"
    val SPOT_GET_URL = "http://$IP_ADDRESS/api/v1/spot/find?user_id="
    val PLAN_POST_URL = "http://$IP_ADDRESS/api/v1/plan/register/"

}