package com.datastax.demo.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

public class ByteUtils {

	public static ByteBuffer toByteBuffer(Object obj) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(obj);
		oos.close();
		return ByteBuffer.wrap(baos.toByteArray());
	}

	public static Object fromByteBuffer(ByteBuffer bytes) throws Exception {
		if ((bytes == null) || !bytes.hasRemaining()) {
			return null;
		}
		int l = bytes.remaining();
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes.array(), bytes.arrayOffset() + bytes.position(), l);
		ObjectInputStream ois;

		ois = new ObjectInputStream(bais);
		Object obj = ois.readObject();
		bytes.position(bytes.position() + (l - ois.available()));
		ois.close();
		return obj;
	}
	
	public static byte[] toBytes(Object obj) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(obj);
		oos.close();
		return baos.toByteArray();
	}
	
	public static Object toObject(byte[] bytes) throws IOException, ClassNotFoundException {
        Object obj = null;
        ByteArrayInputStream bis = null;
        ObjectInputStream ois = null;
        try {
            bis = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(bis);
            obj = ois.readObject();
        } finally {
            if (bis != null) {
                bis.close();
            }
            if (ois != null) {
                ois.close();
            }
        }
        return obj;
    }
}
