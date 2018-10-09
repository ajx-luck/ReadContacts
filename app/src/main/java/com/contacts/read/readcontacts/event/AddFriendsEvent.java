package com.contacts.read.readcontacts.event;

public class AddFriendsEvent {
    public boolean isAddFriendsStart = false;
    public boolean isSendPYQ = false;
    public boolean isSendMSG = false;
    public boolean isDeleteContacts = false;
    public boolean isgetContacts = false;
    public boolean isuploadMSG = false;
    //添加好友间隔时间
    public long delaytime = 2;

    //添加好友间隔时间
    public int addnub = 6;

    //添加好友间隔时间
    public int addrepeat = 3;

    public String displayName = "";

    public String sendnickname;

    public String messagecontent;
    //性别 0为全部 1为男 2为女
    public int sex = 0;
}
