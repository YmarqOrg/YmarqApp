package com.ymarq.eu.ymarq;

/**
 * Created by eu on 11/9/2014.
 */
public class DataProduct {

        private String Description;
        private String Hashtag;
        private String Id;
        private String Image;
        private String PublisherId;
        //Get & Set methods for each field

    public String getDescription ( )
    {
        return Description;
    }

    public void setName (String description)
    {
        Description = description;
    }

    public String getHashtag ( )
    {
        return Hashtag;
    }

    public void setHashtag (String hashtag)
    {
        Hashtag = hashtag;
    }

    public String getId ( )
    {
        return Id;
    }

    public void setId (String id)
    {
        Id = id;
    }

    public String getImage ( )
    {
        return Image;
    }

    public void setImage (String image)
    {
        Image = image;
    }

    public String getPublisherId ( )
    {
        return PublisherId;
    }

    public void setPublisherId (String publisherId)
    {
        PublisherId = publisherId;
    }
}
