package com.apayen.ft_hangouts;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import java.util.Objects;

public class ContactModel implements Parcelable
{
    //////////////////////////////
    // Variables
    //////////////////////////////
    private final int id;
    private final String photo;
    private final String name;
    private final String phone;
    private final String email;
    private final String street;
    private final String city;
    private final String zip;
    private final String notes;
    //////////////////////////////
    // Constructors
    //////////////////////////////
    public ContactModel(int id, String photo, String name, String phone, String email, String street, String city, String zip, String notes)
    {
        this.id = id;
        this.photo = photo;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.street = street;
        this.city = city;
        this.zip = zip;
        this.notes = notes;
    }
    protected ContactModel(Parcel in)
    {
        id = in.readInt();
        photo = in.readString();
        name = in.readString();
        phone = in.readString();
        email = in.readString();
        street = in.readString();
        city = in.readString();
        zip = in.readString();
        notes = in.readString();
    }
    //////////////////////////////
    // Parcel
    //////////////////////////////
    public static final Creator<ContactModel> CREATOR = new Creator<>()
    {
		@Override
		public ContactModel createFromParcel(Parcel in) { return new ContactModel(in); }
		@Override
		public ContactModel[] newArray(int size) { return new ContactModel[size]; }
	};
    @Override
    public int describeContents()
    { return (0); }
    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(id);
        dest.writeString(photo);
        dest.writeString(name);
        dest.writeString(phone);
        dest.writeString(email);
        dest.writeString(street);
        dest.writeString(city);
        dest.writeString(zip);
        dest.writeString(notes);
    }
    //////////////////////////////
    // Getters
    //////////////////////////////
    public int getID() { return (id); }
    public String getPhoto() { return (photo); }
    public String getName() { return (name); }
    public String getPhone() { return (phone); }
    public String getEmail() { return (email); }
    public String getStreet() { return (street); }
    public String getCity() { return (city); }
    public String getZip() { return (zip); }
    public String getNotes() { return (notes); }
    //////////////////////////////
    // Functions
    //////////////////////////////
    @NonNull
    @Override
    public String toString()
    {
        return ("ContactModel{" +
                "id=" + id +
                ", photo='" + photo + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", street='" + street + '\'' +
                ", city='" + city + '\'' +
                ", zip='" + zip + '\'' +
                ", notes='" + notes + '\'' +
                '}');
    }
    public boolean equals(ContactModel cm)
    {
        return (this.id != cm.getID()
                || !Objects.equals(this.photo, cm.getPhoto())
                || !Objects.equals(this.name, cm.getName())
                || !Objects.equals(this.phone, cm.getPhone())
                || !Objects.equals(this.email, cm.getEmail())
                || !Objects.equals(this.street, cm.getStreet())
                || !Objects.equals(this.city, cm.getCity())
                || !Objects.equals(this.zip, cm.getZip())
                || !Objects.equals(this.notes, cm.getNotes()));

    }
}
