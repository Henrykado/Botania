package vazkii.botania.common.lexicon.page;

import thaumcraft.api.crafting.InfusionRecipe;
import vazkii.botania.api.recipe.RecipeRuneAltar;

/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 *
 * File Created @ [Feb 8, 2014, 1:11:35 PM (GMT)]
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.oredict.OreDictionary;

import org.lwjgl.opengl.GL11;

import vazkii.botania.api.internal.IGuiLexiconEntry;
import vazkii.botania.api.lexicon.LexiconEntry;
import vazkii.botania.api.lexicon.LexiconRecipeMappings;
import vazkii.botania.api.recipe.RecipePetals;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.lib.LibResources;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.core.handler.ConfigHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PageInfusionRecipe extends PageRecipe {

    private static final ResourceLocation petalOverlay = new ResourceLocation(LibResources.GUI_PETAL_OVERLAY);

    InfusionRecipe recipe;
    int ticksElapsed = 0;
    int recipeAt = 0;
    int oredictCounter = 0;

    public PageInfusionRecipe(String unlocalizedName, InfusionRecipe recipe) {
        super(unlocalizedName);
        this.recipe = recipe;
    }

    @Override
    public void onPageAdded(LexiconEntry entry, int index) {
        if (recipe != null && recipe.getRecipeOutput() instanceof ItemStack)
            LexiconRecipeMappings.map((ItemStack) recipe.getRecipeOutput(), entry, index);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderRecipe(IGuiLexiconEntry gui, int mx, int my) {
        if (recipe == null || !(recipe.getRecipeOutput() instanceof ItemStack)) return;

        TextureManager render = Minecraft.getMinecraft().renderEngine;

        renderItemAtGridPos(gui, 3, 0, (ItemStack) recipe.getRecipeOutput(), false);
        renderItemAtGridPos(gui, 2, 1, recipe.getRecipeInput(), false);

        //renderItemAtGridPos(gui, 2, 1, getMiddleStack(), false);

        int degreePerInput = (int) (360F / recipe.getComponents().length);
        float currentDegree = ConfigHandler.lexiconRotatingItems ? GuiScreen.isShiftKeyDown() ? ticksElapsed : (float) (ticksElapsed + ClientTickHandler.partialTicks) : 0;

        for(Object obj : recipe.getComponents()) {
            Object input = obj;
            if(input instanceof String) {
                List<ItemStack> ores = OreDictionary.getOres((String) input);
                input = ores.get(oredictCounter % ores.size());
            }

            renderItemAtAngle(gui, currentDegree, (ItemStack) input);

            currentDegree += degreePerInput;
        }

        render.bindTexture(petalOverlay);

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1F, 1F, 1F, 1F);
        ((GuiScreen) gui).drawTexturedModalRect(gui.getLeft(), gui.getTop(), 0, 0, gui.getWidth(), gui.getHeight());
        GL11.glDisable(GL11.GL_BLEND);
    }

    ItemStack getMiddleStack() {
        return new ItemStack(ModBlocks.altar);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateScreen() {
        if(GuiScreen.isShiftKeyDown())
            return;

        ++ticksElapsed;
    }

    @Override
    public List<ItemStack> getDisplayedRecipes() {
        ArrayList<ItemStack> list = new ArrayList<>();

        Object output = recipe.getRecipeOutput();
        if (output instanceof ItemStack) {
            list.add((ItemStack) output);
        }

        return list;
    }

}

