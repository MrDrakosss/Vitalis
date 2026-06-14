package me.xavi.vitalis.registry;

import me.xavi.vitalis.Vitalis;
import me.xavi.vitalis.block.MedicalCabinetBlock;
import me.xavi.vitalis.menus.ComputerMenu;
import me.xavi.vitalis.menus.MedicalCabinetMenu;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public class ModMenuTypes {
    public static final MenuType<MedicalCabinetMenu> MEDICAL_REFRIGERATOR =
            register("medical_refrigerator", MedicalCabinetBlock.CabinetType.REFRIGERATOR, 2);

    public static final MenuType<MedicalCabinetMenu> SUPPLY_CABINET =
            register("supply_cabinet", MedicalCabinetBlock.CabinetType.SUPPLY, 2);

    public static final MenuType<MedicalCabinetMenu> EQUIPMENT_CABINET =
            register("equipment_cabinet", MedicalCabinetBlock.CabinetType.EQUIPMENT, 2);

    public static final MenuType<MedicalCabinetMenu> LARGE_MEDICAL_REFRIGERATOR =
            register("large_medical_refrigerator", MedicalCabinetBlock.CabinetType.REFRIGERATOR, 4);

    public static final MenuType<MedicalCabinetMenu> LARGE_SUPPLY_CABINET =
            register("large_supply_cabinet", MedicalCabinetBlock.CabinetType.SUPPLY, 4);

    public static final MenuType<MedicalCabinetMenu> LARGE_EQUIPMENT_CABINET =
            register("large_equipment_cabinet", MedicalCabinetBlock.CabinetType.EQUIPMENT, 4);

    public static final MenuType<ComputerMenu> COMPUTER =
            Registry.register(
                    BuiltInRegistries.MENU,
                    ResourceLocation.fromNamespaceAndPath(Vitalis.MOD_ID, "computer"),
                    new MenuType<>(
                            ComputerMenu::new,
                            FeatureFlags.DEFAULT_FLAGS
                    )
            );

    private static MenuType<MedicalCabinetMenu> register(
            String name,
            MedicalCabinetBlock.CabinetType cabinetType,
            int rows
    ) {
        return Registry.register(
                BuiltInRegistries.MENU,
                ResourceLocation.fromNamespaceAndPath(Vitalis.MOD_ID, name),
                new MenuType<>(
                        (syncId, inventory) -> new MedicalCabinetMenu(
                                syncId,
                                inventory,
                                new SimpleContainer(rows * 9),
                                rows,
                                cabinetType
                        ),
                        FeatureFlags.DEFAULT_FLAGS
                )
        );
    }

    public static void register() {
    }
}
