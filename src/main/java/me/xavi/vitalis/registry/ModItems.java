package me.xavi.vitalis.registry;

import me.xavi.vitalis.Vitalis;
import me.xavi.vitalis.item.AdrenalineItem;
import me.xavi.vitalis.item.EnergyDrinkItem;
import me.xavi.vitalis.item.MedicalItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

public final class ModItems {

    public static List<Item> ITEMS = new ArrayList<>();

    public static final Item BANDAGE = register(
            "bandage",
            new MedicalItem(
                    new Item.Properties().stacksTo(64),
                    "item.vitalis.bandage.tooltip"
            )
    );

    public static final Item STERILE_BANDAGE = register(
            "sterile_bandage",
            new MedicalItem(
                    new Item.Properties().stacksTo(64),
                    "item.vitalis.sterile_bandage.tooltip"
            )
    );

    public static final Item PRESSURE_BANDAGE = register(
            "pressure_bandage",
            new MedicalItem(
                    new Item.Properties().stacksTo(32),
                    "item.vitalis.pressure_bandage.tooltip"
            )
    );

    public static final Item TOURNIQUET = register(
            "tourniquet",
            new MedicalItem(
                    new Item.Properties().stacksTo(16),
                    "item.vitalis.tourniquet.tooltip"
            )
    );

    public static final Item SPLINT = register(
            "splint",
            new MedicalItem(
                    new Item.Properties().stacksTo(16),
                    "item.vitalis.splint.tooltip"
            )
    );

    public static final Item CAST = register(
            "cast",
            new MedicalItem(
                    new Item.Properties().stacksTo(16),
                    "item.vitalis.cast.tooltip"
            )
    );

    public static final Item SURGICAL_KIT = register(
            "surgical_kit",
            new MedicalItem(
                    new Item.Properties().stacksTo(8),
                    "item.vitalis.surgical_kit.tooltip"
            )
    );

    public static final Item SCALPEL = register(
            "scalpel",
            new MedicalItem(
                    new Item.Properties().stacksTo(1),
                    "item.vitalis.scalpel.tooltip"
            )
    );

    public static final Item FORCEPS = register(
            "forceps",
            new MedicalItem(
                    new Item.Properties().stacksTo(1),
                    "item.vitalis.forceps.tooltip"
            )
    );

    public static final Item SUTURE_KIT = register(
            "suture_kit",
            new MedicalItem(
                    new Item.Properties().stacksTo(16),
                    "item.vitalis.suture_kit.tooltip"
            )
    );

    public static final Item PAINKILLER = register(
            "painkiller",
            new MedicalItem(
                    new Item.Properties().stacksTo(16),
                    "item.vitalis.painkiller.tooltip"
            )
    );

    public static final Item MORPHINE = register(
            "morphine",
            new MedicalItem(
                    new Item.Properties().stacksTo(8),
                    "item.vitalis.morphine.tooltip"
            )
    );

    public static final Item ANTIBIOTIC = register(
            "antibiotic",
            new MedicalItem(
                    new Item.Properties().stacksTo(16),
                    "item.vitalis.antibiotic.tooltip"
            )
    );

    public static final Item BLOOD_BAG = register(
            "blood_bag",
            new MedicalItem(
                    new Item.Properties().stacksTo(8),
                    "item.vitalis.blood_bag.tooltip"
            )
    );

    public static final Item IV_SET = register(
            "iv_set",
            new MedicalItem(
                    new Item.Properties().stacksTo(8),
                    "item.vitalis.iv_set.tooltip"
            )
    );

    public static final Item VITAL_SCANNER = register(
            "vital_scanner",
            new MedicalItem(
                    new Item.Properties().stacksTo(1),
                    "item.vitalis.vital_scanner.tooltip"
            )
    );

    public static final Item ENERGY_DRINK = register(
            "energy_drink",
            new EnergyDrinkItem(
                    new Item.Properties().stacksTo(16)
            )
    );

    public static final Item ADRENALINE_INJECTION = register(
            "adrenaline_injection",
            new AdrenalineItem(
                    new Item.Properties().stacksTo(8)
            )
    );

    public static final Item DEFIBRILLATOR = register(
            "defibrillator",
            new MedicalItem(
                    new Item.Properties().stacksTo(1).durability(16),
                    "item.vitalis.defibrillator.tooltip"
            )
    );

    public static final Item CPR_KIT = register(
            "cpr_kit",
            new MedicalItem(
                    new Item.Properties().stacksTo(4),
                    "item.vitalis.cpr_kit.tooltip"
            )
    );

    public static final Item OXYGEN_MASK = register(
            "oxygen_mask",
            new MedicalItem(
                    new Item.Properties().stacksTo(8),
                    "item.vitalis.oxygen_mask.tooltip"
            )
    );

    private ModItems() {
    }

    private static Item register(String name, Item item) {
        Item i = Registry.register(
                BuiltInRegistries.ITEM,
                Vitalis.id(name),
                item
        );
        ITEMS.add(i);
        return i;
    }

    public static void initialize() {
        Vitalis.LOGGER.info("Registering Vitalis items and blocks");
    }
}