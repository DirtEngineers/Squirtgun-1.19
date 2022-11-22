package net.dirtengineers.squirtgun.common.item.Squirtgun;

import com.mojang.blaze3d.platform.InputConstants;
import com.smashingmods.chemlib.api.Chemical;
import net.dirtengineers.squirtgun.Constants;
import net.dirtengineers.squirtgun.client.Keybinds;
import net.dirtengineers.squirtgun.client.capabilities.SquirtgunCapabilities;
import net.dirtengineers.squirtgun.client.capabilities.squirtgun.AmmunitionCapabilityProvider;
import net.dirtengineers.squirtgun.client.capabilities.squirtgun.IAmmunitionCapability;
import net.dirtengineers.squirtgun.client.screens.ModScreens;
import net.dirtengineers.squirtgun.common.entity.SquirtSlug;
import net.dirtengineers.squirtgun.common.item.ChemicalPhial;
import net.dirtengineers.squirtgun.common.registry.ItemRegistration;
import net.dirtengineers.squirtgun.common.registry.SoundEventRegistration;
import net.dirtengineers.squirtgun.util.TextUtility;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;


public class SquirtgunItem extends BowItem {

    public SquirtgunItem(Properties pProperties) {
        super(pProperties);
    }

    @javax.annotation.Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack pStack, @javax.annotation.Nullable CompoundTag nbt) {
        return new AmmunitionCapabilityProvider(pStack);
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pItemSlot, boolean pIsSelected) {

    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        IAmmunitionCapability ammunitionHandler = pStack.getCapability(SquirtgunCapabilities.SQUIRTGUN_AMMO).orElse(null);
        pTooltipComponents.add(
                Component.literal(
                                TextUtility.buildAmmoStatus(
                                        ammunitionHandler.getChemical() == null ?
                                                MutableComponent.create(new TranslatableContents(Constants.emptyFluidNameKey)).getString()
                                                : ammunitionHandler.getAmmoStatus()
                                        , MutableComponent.create(new TranslatableContents(this.getDescriptionId())).getString()))
                        .withStyle(Constants.HOVER_TEXT_STYLE));


        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
    }

    private void loadingTest(ItemStack pGunStack) {
        IAmmunitionCapability ammunitionHandler = pGunStack.getCapability(SquirtgunCapabilities.SQUIRTGUN_AMMO, null).orElse(null);
        ammunitionHandler.setChemical((Chemical) ForgeRegistries.ITEMS.getValue(new ResourceLocation("chemlib:hydrochloric_acid")));
        ammunitionHandler.setShotsAvailable(6);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {

        ItemStack itemstack = pPlayer.getItemInHand(pHand);

        loadingTest(itemstack);
        // Only perform the shift action
        if (pPlayer.isShiftKeyDown()) {
//            if (!pLevel.isClientSide) {
//                GunProperties.setCanFire(itemstack, true);
//            }

            if (pLevel.isClientSide) {
                if (Keybinds.shiftClickGuiBinding.getKey() == InputConstants.UNKNOWN) {
                    ModScreens.openGunSettingsScreen();
                    return InteractionResultHolder.pass(itemstack);
                }
            }

            // INTENTIONALLY LEFT IN. I DON'T HAVE THE TIME TO FIX THIS ISSUE ATM
            // @todo: migrate keybinding setting onto gadget so I can set a tag on the item
            return InteractionResultHolder.pass(itemstack);
        }

        boolean hasAmmo = itemstack.getCapability(SquirtgunCapabilities.SQUIRTGUN_AMMO, null).orElse(null).hasAmmunition();

        net.minecraftforge.common.ForgeHooks.getProjectile(pPlayer, itemstack, hasAmmo ? new ItemStack(ItemRegistration.SQUIRTSLUGITEM.get()) : ItemStack.EMPTY);
        InteractionResultHolder<ItemStack> ret = net.minecraftforge.event.ForgeEventFactory.onArrowNock(itemstack, pLevel, pPlayer, pHand, hasAmmo);
        if (ret != null) return ret;

        if (!pPlayer.getAbilities().instabuild && !hasAmmo) {
            return InteractionResultHolder.fail(itemstack);
        } else {
            pPlayer.startUsingItem(pHand);
            pLevel.playSound(
                    pPlayer,
                    pPlayer.getX(),
                    pPlayer.getY(),
                    pPlayer.getZ(),
                    SoundEventRegistration.GUN_USE.get(),
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F / (pLevel.getRandom().nextFloat() * 0.4F + 1.2F) + 0.5F
            );
            return InteractionResultHolder.consume(itemstack);
        }
    }

    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving, int pTimeLeft) {
        if (pEntityLiving instanceof Player player) {

            boolean flag = player.getAbilities().instabuild || EnchantmentHelper.getTagEnchantmentLevel(Enchantments.INFINITY_ARROWS, pStack) > 0;

            boolean hasAmmo = pStack.getCapability(SquirtgunCapabilities.SQUIRTGUN_AMMO, null).orElse(null).hasAmmunition();
            net.minecraftforge.common.ForgeHooks.getProjectile(pEntityLiving, pStack, hasAmmo ? new ItemStack(ItemRegistration.SQUIRTSLUGITEM.get()) : ItemStack.EMPTY);

            int i = getUseDuration(pStack) - pTimeLeft;
            i = net.minecraftforge.event.ForgeEventFactory.onArrowLoose(pStack, pLevel, player, i, hasAmmo || flag);
            if (i < 0) return;

//            Infinity slugs?
//            boolean bInfinityAmmo = bInstabuild || (itemstack.getItem() instanceof SquirtSlugItem && ((SquirtSlugItem)itemstack.getItem()).isInfinite(itemstack, pStack, player));

            if (hasAmmo) {
                if (!pLevel.isClientSide) {
                    SquirtSlug slug = makeSlugToFire(pStack, pLevel, player);
                    if (slug != null) {
                        if (slug.hasEffects()) {
                            slug.setBaseDamage(0D);
                        }
                        slug.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.0F, 1.0F);
                        pLevel.addFreshEntity(slug);
                    }
                }
                pLevel.playSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        SoundEventRegistration.GUN_FIRE.get(),
                        SoundSource.PLAYERS,
                        1.0F,
                        1.0F / (pLevel.getRandom().nextFloat() * 0.4F + 1.2F) + 0.5F
                );
                player.awardStat(Stats.ITEM_USED.get(this));
            }
        }
    }

    public SquirtSlug makeSlugToFire(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving) {
        if (pStack.getItem() instanceof SquirtgunItem) {
            IAmmunitionCapability ammunitionHandler = pStack.getCapability(SquirtgunCapabilities.SQUIRTGUN_AMMO, null).orElse(null);
            if (ammunitionHandler.getChemical() == null) {
                return null;
            }
            ammunitionHandler.decrementShots();
            return new SquirtSlug(pEntityLiving, pLevel, ammunitionHandler.getChemical());
        }
        return null;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.BOW;
    }

    public static ItemStack getPlayerGun(Player player) {
        ItemStack heldItem = ItemStack.EMPTY;
        if (player != null) {
            heldItem = player.getMainHandItem();
            if (!(heldItem.getItem() instanceof SquirtgunItem)) {
                heldItem = player.getOffhandItem();
                if (!(heldItem.getItem() instanceof SquirtgunItem)) {
                    return ItemStack.EMPTY;
                }
            }
        }
        return heldItem;
    }

    public static ItemStack loadNewPhial(Player pPlayer, ItemStack pPhialStack) {
        IAmmunitionCapability ammunitionHandler = getPlayerGun(pPlayer).getCapability(SquirtgunCapabilities.SQUIRTGUN_AMMO, null).orElse(null);
        if (pPhialStack.isEmpty() || !(pPhialStack.getItem() instanceof ChemicalPhial chemicalPhial)) {
            return pPhialStack;
        }

        if (!ammunitionHandler.isChemicalValid(chemicalPhial.getChemical()) || chemicalPhial.getChemical() == ammunitionHandler.getChemical()) {
            return pPhialStack;
        }
        ammunitionHandler.setChemical(chemicalPhial.getChemical());
        ammunitionHandler.setShotsAvailable(chemicalPhial.getShotsAvailable());
        return new ItemStack(ForgeRegistries.ITEMS.getValue(ItemRegistration.PHIAL.getId()), 1);
    }
}