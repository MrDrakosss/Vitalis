package me.xavi.vitalis.registry;

import me.xavi.vitalis.Vitalis;
import me.xavi.vitalis.item.AdrenalineItem;
import me.xavi.vitalis.item.EnergyDrinkItem;
import me.xavi.vitalis.item.MedicalItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public final class ModItems {

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

    public static final Item ADRENALINE = register(
            "adrenaline",
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
        return Registry.register(
                BuiltInRegistries.ITEM,
                Vitalis.id(name),
                item
        );
    }

    public static void initialize() {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
            entries.accept(BANDAGE);
            entries.accept(STERILE_BANDAGE);
            entries.accept(PRESSURE_BANDAGE);
            entries.accept(TOURNIQUET);
            entries.accept(SPLINT);
            entries.accept(CAST);
            entries.accept(SURGICAL_KIT);
            entries.accept(SCALPEL);
            entries.accept(FORCEPS);
            entries.accept(SUTURE_KIT);
            entries.accept(PAINKILLER);
            entries.accept(MORPHINE);
            entries.accept(ANTIBIOTIC);
            entries.accept(BLOOD_BAG);
            entries.accept(IV_SET);
            entries.accept(VITAL_SCANNER);
            entries.accept(ENERGY_DRINK);
            entries.accept(ADRENALINE);
            entries.accept(DEFIBRILLATOR);
            entries.accept(CPR_KIT);
            entries.accept(OXYGEN_MASK);
        });
    }
}