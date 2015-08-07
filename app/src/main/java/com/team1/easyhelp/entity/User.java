package com.team1.easyhelp.entity;
/**
 * Created by thetruthmyg on 2015/7/27.
 */
public class User {

    private int id;
    private String name;
    private String nickname;
    private int gender;
    private int age;
    private String phone;
    private String location;
    private double longitude;
    private double latitude;
    private int occupation;
    private double reputation;
    private String identity_id;
    private int isVerify;


//    private HealthCard healthCard;

    public void setOccupation(int occupation) {
        this.occupation = occupation;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public void setIdentity_id(String identity_id) {
        this.identity_id = identity_id;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setReputation(double reputation) {
        this.reputation = reputation;
    }

    public void setIsVerify(int isVerify) {
        this.isVerify = isVerify;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAge(int age) {
        this.age = age;
    }

//    public void setHealthCard(HealthCard healthCard) { this.healthCard = healthCard; }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getOccupation() {
        return occupation;
    }

    public int getGender() {
        return gender;
    }

    public String getIdentity_id() {
        return identity_id;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getReputation() {
        return reputation;
    }

    public int getIsVerify() {
        return isVerify;
    }

    public String getPhone() {
        return phone;
    }

    public String getName() {
        return name;
    }

    public String getNickname() {
        return nickname;
    }

    public String getLocation() {
        return location;
    }

    public int getId() {
        return id;
    }

    public int getAge() {
        return age;
    }

    public double getLongitude() {
        return longitude;
    }

//    public HealthCard getHealthCard() { return healthCard; }
}
