package com.gymdatapro.gymmanagementsystm

data class UserData(
    val id:String?=null,
    val username:String?=null,
    val password:String?=null
)

data class FeeData(
    val id: String?=null,
    val onemonth:String?=null,
    val threemonth:String?=null,
    val sixmonth:String?=null,
    val oneyear:String?=null,
    val threeyear:String?=null
)
data class AddMember(
    var id: String? = null,
    var FName: String? = null,
    var LName: String? = null,
    var Gender: String? = null,
    var Age: String? = null,
    var Weight: String? = null,
    var Mobile: String? = null,
    var DateOfJoining: String? = null,
    var Membership: String? = null,
    var Expire: String? = null,
    var Discount: String? = null,
    var Total: String? = null,
    var Image: String? = null,
)