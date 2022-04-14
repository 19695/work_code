package org.colm.code;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

public class ListCopyUtil {

    public static <E, T> void copyList(List<T> source, List<E> target, Class<E> type) {
        source.stream().forEach(item -> {
            try {
                E e = type.newInstance();
                BeanUtils.copyProperties(item, e);
                target.add(e);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static <E, T> List<E> copyList(List<T> source, Class<E> type) {
        List<E> target = new ArrayList<>();
        source.stream().forEach(item -> {
            try {
                E e = type.newInstance();
                BeanUtils.copyProperties(item, e);
                target.add(e);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
        return target;
    }

}
