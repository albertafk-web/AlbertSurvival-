package com.albertsurvival;

import com.albertsurvival.data.PlayerData;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlayerDataTest {

    @Test
    void newPlayerStartsWithDefaults() {
        UUID id = UUID.randomUUID();
        PlayerData data = new PlayerData(id, 100.0, 0.0);
        assertEquals(100.0, data.getThirst());
        assertEquals(0.0, data.getTemperature());
        assertEquals(id, data.getUuid());
    }

    @Test
    void thirstClampedBySetterLogic() {
        PlayerData data = new PlayerData(UUID.randomUUID(), 50, 0);
        data.setThirst(150); // manager tarafında clamp edilir, burada sadece set
        assertEquals(150, data.getThirst()); // raw set
        data.setThirst(-10);
        assertEquals(-10, data.getThirst());
    }

    @Test
    void dirtyFlagWorks() {
        PlayerData data = new PlayerData(UUID.randomUUID(), 100, 0);
        assertFalse(data.isDirty());
        data.setThirst(80);
        assertTrue(data.isDirty());
        data.markClean();
        assertFalse(data.isDirty());
    }
}
