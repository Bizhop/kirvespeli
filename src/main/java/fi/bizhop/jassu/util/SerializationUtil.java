package fi.bizhop.jassu.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Optional;

public class SerializationUtil {
    public static Optional<byte[]> getByteArrayObject(Object simpleExample){
        byte[] byteArrayObject;
        try(ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos)) {

            oos.writeObject(simpleExample);
            byteArrayObject = bos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
        return Optional.of(byteArrayObject);
    }

    public static <T> Optional<T> getJavaObject(byte[] convertObject, Class<T> type){
        T obj = null;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(convertObject);
            ObjectInputStream ins = new ObjectInputStream(bais)) {

            obj = (T)ins.readObject();
        }
        catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
        return Optional.ofNullable(obj);
    }
}
