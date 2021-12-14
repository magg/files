package com.magg.files.model;


public class Response<T> {
    /** Boolean indicating if request succeeded **/
    private boolean status;

    /** Message indicating error if any **/
    private String message;

    /** Additional data that is part of this response **/
    private T data;

    public Response(boolean status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    // Setters and getters


    public boolean isStatus()
    {
        return status;
    }


    public void setStatus(boolean status)
    {
        this.status = status;
    }


    public String getMessage()
    {
        return message;
    }


    public void setMessage(String message)
    {
        this.message = message;
    }


    public T getData()
    {
        return data;
    }


    public void setData(T data)
    {
        this.data = data;
    }
}
