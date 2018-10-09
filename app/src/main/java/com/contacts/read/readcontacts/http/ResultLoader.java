package com.contacts.read.readcontacts.http;

import java.util.List;

import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;
import retrofit2.http.Url;
import rx.Observable;

public class ResultLoader extends ObjectLoader{
    private static final String NAME_URL = "names";
    private static final String ACCESS_URL = "access";
    private static final String CHECK_URL = "check";
    private static final String PYQINFO_URL = "pyqinfo";
    private static final String NAMES_URL = "names";
    private static final String ADDFRIENDS_URL = "addfriends";
    private static final String STOPALL_URL = "stopall";
    private static final String GETPHONE_URL = "getphone";
    private static final String ADDMESSAGE_URL = "addmessage";
    private ApiService mApiService ;
    public ResultLoader(){
        mApiService = RetrofitServiceManager.getInstance().create(ApiService.class);
    }
    //获取聊天名称列表
    public  Observable<BaseResponse<ChatResp>> getNames(String name,String deviceid){
        return mApiService.names(NAME_URL,name,deviceid);
    }
    //获取聊天名称列表
    public  Observable<BaseResponse<PYQInfo>> getPYQInfo(String name,String deviceid){
        return mApiService.pyqinfo(PYQINFO_URL,name,deviceid);
    }
    //激活
    public Observable<BaseResponse<String>> getAccess(String pushkey,String secret,String device,String name){
        return mApiService.access(ACCESS_URL,pushkey,secret,device,name);
    }
    //验证设备是否到期
    public Observable<BaseResponse<String>> getCheck(String device){
        return mApiService.check(CHECK_URL,device);
    }

    //群发朋友圈
    public Observable<BaseResponse<String>> savepyqinfo(String content,String index,String count,String name,String deviceid){
        return mApiService.savepyqinfo(PYQINFO_URL,content,index,count,name,deviceid);
    }

    //群发养号
    public Observable<BaseResponse<String>> savenames(String names,String name,String deviceid){
        return mApiService.savenames(NAMES_URL,names,name,deviceid);
    }

    //群发加粉
    public Observable<BaseResponse<String>> saveaddfriends(String time,String name,String deviceid,String nub,String repeat,String sex){
        return mApiService.saveaddfriends(ADDFRIENDS_URL,time,name,deviceid,nub,repeat,sex);
    }

    //群发停止
    public Observable<BaseResponse<String>> stopall(String name,String deviceid){
        return mApiService.stopall(STOPALL_URL,name,deviceid);
    }
    //在线获取联系人
    public Observable<BaseResponse<List<ContactsInfo>>> getphone(String username,String deviceid){
        return mApiService.contactsinfo(GETPHONE_URL,username,deviceid);
    }

    public Observable<BaseResponse<String>> addmessage( String sendpushkey,String receivepushkey, String deviceid, String username, String sendnickname,String messagecontent){
        return mApiService.addmessage(ADDMESSAGE_URL,sendpushkey,receivepushkey,deviceid,username,sendnickname,messagecontent);
    }


    public interface ApiService {
        @GET
        Observable<BaseResponse<ChatResp>> names(@Url String url,@Query("name") String name, @Query("deviceid") String deviceid);
        @GET
        Observable<BaseResponse<PYQInfo>> pyqinfo(@Url String url,@Query("name") String name, @Query("deviceid") String deviceid);
        @GET
        Observable<BaseResponse<String>> access(@Url String url, @Query("pushkey") String pushkey,@Query("secret") String secret,@Query("deviceid") String deviceid,@Query("name") String name);
        @GET
        Observable<BaseResponse<String>> check(@Url String url, @Query("deviceid") String deviceid);
        @PUT
        Observable<BaseResponse<String>> savepyqinfo(@Url String url, @Query("content") String content,@Query("index") String index,@Query("count") String count,@Query("name") String name, @Query("deviceid") String deviceid);
        @PUT
        Observable<BaseResponse<String>> savenames(@Url String url, @Query("names") String names,@Query("name") String name, @Query("deviceid") String deviceid);
        @PUT
        Observable<BaseResponse<String>> saveaddfriends(@Url String url, @Query("time") String time,@Query("name") String name, @Query("deviceid") String deviceid,@Query("nub") String nub,@Query("repeat") String repeat,@Query("sex") String sex);
        @GET
        Observable<BaseResponse<String>> stopall(@Url String url,@Query("name") String name, @Query("deviceid") String deviceid);
        @GET
        Observable<BaseResponse<List<ContactsInfo>>> contactsinfo(@Url String url, @Query("username") String username, @Query("deviceid") String deviceid);

        @POST
        Observable<BaseResponse<String>> addmessage(@Url String url, @Query("sendpushkey") String sendpushkey,@Query("receivepushkey") String receivepushkey,@Query("deviceid") String deviceid,@Query("username") String username,@Query("sendnickname") String sendnickname,@Query("messagecontent") String messagecontent);

    }
}
