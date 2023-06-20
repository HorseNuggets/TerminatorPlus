package net.nuggetmc.tplus.utils;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.network.syncher.SynchedEntityData;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class NMSUtils {
    private static String itemsByIdFieldName;

    static {
        // find a private final field in SynchedEntityData that is an Int2ObjectMap<SynchedEntityData.DataItem>
        Class<SynchedEntityData> clazz = SynchedEntityData.class;
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().equals(Int2ObjectMap.class) && Modifier.isPrivate(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
                itemsByIdFieldName = field.getName();
                break;
            }
        }
        if (itemsByIdFieldName == null) {
            throw new RuntimeException("Could not find itemsById field in SynchedEntityData");
        }
    }

    public static List<SynchedEntityData.DataValue<?>> getEntityData(SynchedEntityData synchedEntityData) {
        Int2ObjectMap<SynchedEntityData.DataItem> map = null;
        try {
            Field field = synchedEntityData.getClass().getDeclaredField(itemsByIdFieldName);
            field.setAccessible(true);
            map = (Int2ObjectMap<SynchedEntityData.DataItem>) field.get(synchedEntityData);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        List<SynchedEntityData.DataValue<?>> entityData = new ArrayList<>();
        for (SynchedEntityData.DataItem<?> dataItem : map.values()) {
            entityData.add(dataItem.value());
        }
        return entityData;
    }
}
