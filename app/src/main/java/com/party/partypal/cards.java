package com.party.partypal;

public class cards {
    private String userid;
    private String name;

    public cards(String userid, String name){
        this.userid = userid;
        this.name = name;
    }
    public String getUserid(){
        return userid;
    }
    public void setUserid(String userid){
        this.userid = userid;
    }

    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }
}
