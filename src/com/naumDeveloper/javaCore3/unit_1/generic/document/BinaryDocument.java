package com.naumDeveloper.javaCore3.unit_1.generic.document;


import java.util.ArrayList;

public class BinaryDocument implements IDocument<byte[]> {


    @Override
    public byte[] getHeader() {
        return new byte[0];
    }

    @Override
    public byte[] getContent() {
        return new byte[0];
    }


}
