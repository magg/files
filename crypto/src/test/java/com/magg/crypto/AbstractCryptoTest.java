package com.magg.crypto;

import static javax.xml.bind.DatatypeConverter.parseBase64Binary;

public abstract class AbstractCryptoTest
{

    protected byte[] getIV()
    {
        String initialVectorSource = "ubCFToAcloWSE8HSHgMLwA==";
        return parseBase64Binary(initialVectorSource);
    }
}
