/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 * 
 * File Created @ [Jul 7, 2015, 6:14:18 PM (GMT)]
 */
package vazkii.botania.common.world;

import java.awt.Color;
import java.util.List;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.lexicon.LexiconCategory;
import vazkii.botania.api.lexicon.LexiconEntry;
import vazkii.botania.api.recipe.RecipePetals;
import vazkii.botania.common.Botania;
import vazkii.botania.common.block.ModBlocks;
import vazkii.botania.common.block.tile.TileManaFlame;
import vazkii.botania.common.crafting.ModCraftingRecipes;
import vazkii.botania.common.crafting.ModManaInfusionRecipes;
import vazkii.botania.common.crafting.ModPetalRecipes;
import vazkii.botania.common.item.ModItems;
import vazkii.botania.common.item.block.ItemBlockSpecialFlower;
import vazkii.botania.common.item.equipment.tool.ToolCommons;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import vazkii.botania.common.lexicon.ALexiconEntry;
import vazkii.botania.common.lexicon.BLexiconEntry;
import vazkii.botania.common.lexicon.LexiconData;
import vazkii.botania.common.lexicon.page.PageCraftingRecipe;
import vazkii.botania.common.lexicon.page.PageManaInfusionRecipe;
import vazkii.botania.common.lexicon.page.PagePetalRecipe;
import vazkii.botania.common.lexicon.page.PageText;
import vazkii.botania.common.lib.LibBlockNames;
import vazkii.botania.common.lib.LibLexicon;
import vazkii.botania.common.lib.LibOreDict;

public final class SkyblockWorldEvents {

	private static final String TAG_MADE_ISLAND = "Botania-MadeIsland";
	private static final String TAG_HAS_OWN_ISLAND = "Botania-HasOwnIsland";
	private static final String TAG_ISLAND_X = "Botania-IslandX";
	private static final String TAG_ISLAND_Y = "Botania-IslandY";
	private static final String TAG_ISLAND_Z = "Botania-IslandZ";

	@SubscribeEvent
	public void onPlayerUpdate(LivingUpdateEvent event) {
		if(event.entityLiving instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) event.entityLiving;
			if (player.ticksExisted == 2) {
				updateSkyblockRecipes(WorldTypeSkyblock.isWorldSkyblock(player.worldObj));
			}
			if (!event.entity.worldObj.isRemote) {
				NBTTagCompound data = player.getEntityData();
				if (!data.hasKey(EntityPlayer.PERSISTED_NBT_TAG))
					data.setTag(EntityPlayer.PERSISTED_NBT_TAG, new NBTTagCompound());

				NBTTagCompound persist = data.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
				if (player.ticksExisted > 3 && !persist.getBoolean(TAG_MADE_ISLAND)) {
					World world = player.worldObj;
					if (WorldTypeSkyblock.isWorldSkyblock(world)) {
						ChunkCoordinates coords = world.getSpawnPoint();
						if (world.getBlock(coords.posX, coords.posY - 4, coords.posZ) != Blocks.bedrock && world.provider.dimensionId == 0)
							spawnPlayer(player, coords.posX, coords.posY, coords.posZ, false);
					}

					persist.setBoolean(TAG_MADE_ISLAND, true);
				}
			}
		}
	}

	@SubscribeEvent
	public void onPlayerInteract(PlayerInteractEvent event) {
		if(WorldTypeSkyblock.isWorldSkyblock(event.world)) {
			ItemStack equipped = event.entityPlayer.getCurrentEquippedItem();
			if(event.action == Action.RIGHT_CLICK_BLOCK && equipped == null && event.entityPlayer.isSneaking()) {
				Block block = event.world.getBlock(event.x, event.y, event.z);
				if(block == Blocks.grass || block == Blocks.dirt) {
					if(event.world.isRemote)
						event.entityPlayer.swingItem();
					else {
						event.world.playSoundEffect(event.x + 0.5, event.y + 0.5, event.z + 0.5, block.stepSound.getBreakSound(), block.stepSound.getVolume() * 0.4F, block.stepSound.getPitch() + (float) (Math.random() * 0.2 - 0.1));
						if(Math.random() < 0.8)
							event.entityPlayer.dropPlayerItemWithRandomChoice(new ItemStack(ModItems.manaResource, 1, 21), false);
					}
				}
			} else if(equipped != null && equipped.getItem() == Items.bowl && event.action == Action.RIGHT_CLICK_BLOCK && !event.world.isRemote) {
				MovingObjectPosition movingobjectposition = ToolCommons.raytraceFromEntity(event.world, event.entityPlayer, true, 4.5F);

				if(movingobjectposition != null) {
					if (movingobjectposition.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && !event.world.isRemote) {
						int i = movingobjectposition.blockX;
						int j = movingobjectposition.blockY;
						int k = movingobjectposition.blockZ;

						if(event.world.getBlock(i, j, k).getMaterial() == Material.water) {
							--equipped.stackSize;

							if(equipped.stackSize <= 0)
								event.entityPlayer.inventory.setInventorySlotContents(event.entityPlayer.inventory.currentItem, new ItemStack(ModItems.waterBowl));
							else event.entityPlayer.dropPlayerItemWithRandomChoice(new ItemStack(ModItems.waterBowl), false);
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onDrops(HarvestDropsEvent event) {
		if(WorldTypeSkyblock.isWorldSkyblock(event.world) && event.block == Blocks.tallgrass) {
			ItemStack stackToRemove = null;
			for(ItemStack stack : event.drops)
				if(stack.getItem() == Items.wheat_seeds && event.world.rand.nextInt(4) == 0) {
					stackToRemove = stack;
					break;
				}

			if(stackToRemove != null) {
				event.drops.remove(stackToRemove);
				event.drops.add(new ItemStack(event.world.rand.nextBoolean() ? Items.pumpkin_seeds : Items.melon_seeds));
			}
		}
	}


	public static void addShapelessOreDictRecipe(ItemStack output, Object... recipe) {
		CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(output, recipe));
	}
	public static void addShapedOreDictRecipe(ItemStack output, Object... recipe) {
		CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(output, recipe));
	}

	public static void removeRecipe(IRecipe recipe) {
		if (recipe != null) CraftingManager.getInstance().getRecipeList().remove(recipe);
	}
	public static void removePetalRecipe(RecipePetals recipe) {
		if (recipe != null) BotaniaAPI.petalRecipes.remove(recipe);
	}
	public static void removeRecipes(List<IRecipe> recipe) {
		if (recipe != null) CraftingManager.getInstance().getRecipeList().removeAll(recipe);
	}
	public static void removeEntry(LexiconEntry entry) {
		if (entry != null) {
			BotaniaAPI.getAllEntries().remove(entry);
			entry.category.entries.remove(entry);
		}
	}

	public static void updateSkyblockRecipes(boolean isWorldSkyblock) {
		removeRecipe(ModCraftingRecipes.recipeFertilizerPowder);
		removeRecipe(ModCraftingRecipes.recipeCocoon);
		removeRecipe(ModCraftingRecipes.recipeBlazeBlock);
		removeRecipe(ModCraftingRecipes.recipeFromBlazeBlock);
		removeRecipe(ModCraftingRecipes.recipeMagmaToSlimeball);
		removeRecipe(ModCraftingRecipes.recipeEndPortal);
		removeRecipes(ModCraftingRecipes.recipesSpreader);

		removePetalRecipe(ModPetalRecipes.orechidRecipe);
		if (ModManaInfusionRecipes.sugarCaneRecipe != null)
			BotaniaAPI.manaInfusionRecipes.remove(ModManaInfusionRecipes.sugarCaneRecipe);

		removeEntry(LexiconData.gardenOfGlass);
		removeEntry(LexiconData.orechid);
		removeEntry(LexiconData.cocoon);


		if (isWorldSkyblock) {
			// Mana Spreader
			for (int i = 0; i < 16; i++) {
				CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(
						new ItemStack(ModBlocks.spreader), "WWW", "GP ", "WWW",
						'W', LibOreDict.LIVING_WOOD,
						'P', LibOreDict.PETAL[i],
						'G', LibOreDict.LIVING_WOOD));
			}
			ModCraftingRecipes.recipesSpreader = BotaniaAPI.getLatestAddedRecipes(16);

			// Floral Fertilizer
			CraftingManager.getInstance().addShapelessRecipe(new ItemStack(ModItems.fertilizer, 3), new ItemStack(Items.dye, 1, 15), new ItemStack(ModItems.dye, 1, Short.MAX_VALUE), new ItemStack(ModItems.dye, 1, Short.MAX_VALUE), new ItemStack(ModItems.dye, 1, Short.MAX_VALUE), new ItemStack(ModItems.dye, 1, Short.MAX_VALUE));
			ModCraftingRecipes.recipeFertilizerPowder = BotaniaAPI.getLatestAddedRecipe();

			// Cocoon of Caprice
			addShapedOreDictRecipe(new ItemStack(ModBlocks.cocoon),
					"SSS", "SFS", "SIS",
					'S', new ItemStack(Items.string),
					'F', new ItemStack(ModBlocks.felPumpkin),
					'I', LibOreDict.MANA_STEEL);
			ModCraftingRecipes.recipeCocoon = BotaniaAPI.getLatestAddedRecipe();

			// Blaze Light
			addShapedOreDictRecipe(new ItemStack(ModBlocks.blazeBlock),
					"BBB", "BBB", "BBB",
					'B', "powderBlaze"); //"rodBlaze"
			ModCraftingRecipes.recipeBlazeBlock = BotaniaAPI.getLatestAddedRecipe();

			addShapelessOreDictRecipe(new ItemStack(Items.blaze_powder, 9), LibOreDict.BLAZE_BLOCK); //Items.blaze_rod
			ModCraftingRecipes.recipeFromBlazeBlock = BotaniaAPI.getLatestAddedRecipe();

			// Orechid
			ModPetalRecipes.orechidRecipe = BotaniaAPI.registerPetalRecipe(ItemBlockSpecialFlower.ofType(LibBlockNames.SUBTILE_ORECHID), ModPetalRecipes.gray, ModPetalRecipes.gray, ModPetalRecipes.yellow, ModPetalRecipes.yellow, ModPetalRecipes.green, ModPetalRecipes.green, ModPetalRecipes.red, ModPetalRecipes.red);


			// Magma Pearl to Slimeball
			addShapelessOreDictRecipe(new ItemStack(Items.slime_ball), new ItemStack(Items.magma_cream), new ItemStack(Items.water_bucket));
			ModCraftingRecipes.recipeMagmaToSlimeball = BotaniaAPI.getLatestAddedRecipe();

			// Ender Portal
			addShapedOreDictRecipe(new ItemStack(Blocks.end_portal_frame),
					"OGO",
					'O', new ItemStack(Blocks.obsidian),
					'G', LibOreDict.LIFE_ESSENCE);
			ModCraftingRecipes.recipeEndPortal = BotaniaAPI.getLatestAddedRecipe();

			ModManaInfusionRecipes.sugarCaneRecipe = BotaniaAPI.registerManaInfusionRecipe(new ItemStack(Items.reeds), new ItemStack(Blocks.hay_block), 2000);


			LexiconData.gardenOfGlass = new BLexiconEntry(LibLexicon.BASICS_GARDEN_OF_GLASS, BotaniaAPI.categoryBasics);
			LexiconData.gardenOfGlass.setLexiconPages(new PageText("0"), new PageText("1"), new PageText("2"),
					new PageCraftingRecipe("3", ModCraftingRecipes.recipeRootToSapling),
					new PageCraftingRecipe("4", ModCraftingRecipes.recipeRootToFertilizer),
					new PageCraftingRecipe("5", ModCraftingRecipes.recipePebbleCobblestone), new PageText("6"),
					new PageManaInfusionRecipe("7", ModManaInfusionRecipes.sugarCaneRecipe),
					new PageCraftingRecipe("8", ModCraftingRecipes.recipeMagmaToSlimeball), new PageText("9"),
					new PageText("11"), new PageCraftingRecipe("12", ModCraftingRecipes.recipeEndPortal));
			LexiconData.gardenOfGlass.setPriority().setIcon(new ItemStack(ModItems.manaResource, 1, 20));

			LexiconData.orechid = new BLexiconEntry(LibLexicon.FFLOWER_ORECHID, BotaniaAPI.categoryFunctionalFlowers);
			LexiconData.orechid.setLexiconPages(new PageText("0"), new PagePetalRecipe<>("1", ModPetalRecipes.orechidRecipe));
			LexiconData.orechid.setPriority();

			LexiconData.cocoon = new BLexiconEntry(LibLexicon.DEVICE_COCOON, BotaniaAPI.categoryDevices);
			LexiconData.cocoon.setLexiconPages(new PageText("0"), new PageText("1"),
					new PageCraftingRecipe("2", ModCraftingRecipes.recipeCocoon));
		}
		else {
			// Mana Spreader
			for (int i = 0; i < 16; i++) {
				CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(
						new ItemStack(ModBlocks.spreader),
						"WWW", "GP ", "WWW",
						'W', LibOreDict.LIVING_WOOD,
						'P', LibOreDict.PETAL[i],
						'G', "ingotGold"));
			}
			ModCraftingRecipes.recipesSpreader = BotaniaAPI.getLatestAddedRecipes(16);

			// Floral Fertilizer
			CraftingManager.getInstance().addShapelessRecipe(new ItemStack(ModItems.fertilizer, 1), new ItemStack(Items.dye, 1, 15), new ItemStack(ModItems.dye, 1, Short.MAX_VALUE), new ItemStack(ModItems.dye, 1, Short.MAX_VALUE), new ItemStack(ModItems.dye, 1, Short.MAX_VALUE), new ItemStack(ModItems.dye, 1, Short.MAX_VALUE));
			ModCraftingRecipes.recipeFertilizerPowder = BotaniaAPI.getLatestAddedRecipe();

			// Cocoon of Caprice
			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ModBlocks.cocoon),
					"SSS", "SPS", "SDS",
					'S', new ItemStack(Items.string),
					'P', LibOreDict.PIXIE_DUST,
					'D', LibOreDict.DRAGONSTONE));
			ModCraftingRecipes.recipeCocoon = BotaniaAPI.getLatestAddedRecipe();

			// Blaze Light
			CraftingManager.getInstance().getRecipeList().add(new ShapedOreRecipe(new ItemStack(ModBlocks.blazeBlock),
					"BBB", "BBB", "BBB",
					'B', "powderBlaze")); //"rodBlaze"
			ModCraftingRecipes.recipeBlazeBlock = BotaniaAPI.getLatestAddedRecipe();

			CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(Items.blaze_powder, 9), LibOreDict.BLAZE_BLOCK)); //Items.blaze_rod
			ModCraftingRecipes.recipeFromBlazeBlock = BotaniaAPI.getLatestAddedRecipe();

			// Orechid
			ModPetalRecipes.orechidRecipe = BotaniaAPI.registerPetalRecipe(ItemBlockSpecialFlower.ofType(LibBlockNames.SUBTILE_ORECHID), ModPetalRecipes.gray, ModPetalRecipes.gray, ModPetalRecipes.yellow, ModPetalRecipes.green, ModPetalRecipes.red, ModPetalRecipes.runePride, ModPetalRecipes.runeGreed, ModPetalRecipes.redstoneRoot, ModPetalRecipes.pixieDust);


			LexiconData.gardenOfGlass = null;

			LexiconData.orechid = new ALexiconEntry(LibLexicon.FFLOWER_ORECHID, BotaniaAPI.categoryFunctionalFlowers);
			LexiconData.orechid.setLexiconPages(new PageText("0"), new PagePetalRecipe<>("1", ModPetalRecipes.orechidRecipe));

			LexiconData.cocoon = new ALexiconEntry(LibLexicon.DEVICE_COCOON, BotaniaAPI.categoryDevices);
			LexiconData.cocoon.setLexiconPages(new PageText("0"), new PageText("1"),
					new PageCraftingRecipe("2", ModCraftingRecipes.recipeCocoon));
		}
	}
	
	public static void spawnPlayer(EntityPlayer player, int x, int y, int z, boolean fabricated) {
		NBTTagCompound data = player.getEntityData();
		if(!data.hasKey(EntityPlayer.PERSISTED_NBT_TAG))
			data.setTag(EntityPlayer.PERSISTED_NBT_TAG, new NBTTagCompound());
		NBTTagCompound persist = data.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);

		final boolean test = false;

		if(test || !persist.getBoolean(TAG_HAS_OWN_ISLAND)) {
			createSkyblock(player.worldObj, x, y, z);

			if(player instanceof EntityPlayerMP) {
				EntityPlayerMP pmp = (EntityPlayerMP) player;
				pmp.setPositionAndUpdate(x + 0.5, y + 1.6, z + 0.5);
				pmp.setSpawnChunk(new ChunkCoordinates(x, y, z), true);
				player.inventory.addItemStackToInventory(new ItemStack(ModItems.lexicon));
			}

			if(fabricated) {
				persist.setBoolean(TAG_HAS_OWN_ISLAND, true);
				persist.setDouble(TAG_ISLAND_X, player.posX);
				persist.setDouble(TAG_ISLAND_Y, player.posY);
				persist.setDouble(TAG_ISLAND_Z, player.posZ);
			}
		} else {
			double posX = persist.getDouble(TAG_ISLAND_X);
			double posY = persist.getDouble(TAG_ISLAND_Y);
			double posZ = persist.getDouble(TAG_ISLAND_Z);

			if(player instanceof EntityPlayerMP) {
				EntityPlayerMP pmp = (EntityPlayerMP) player;
				pmp.setPositionAndUpdate(posX, posY, posZ);
			}
		}
	}

	public static void createSkyblock(World world, int x, int y, int z) {
		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 4; j++)
				for(int k = 0; k < 3; k++)
					world.setBlock(x - 1 + i, y - 1 - j, z - 1 + k, j == 0 ? Blocks.grass : Blocks.dirt);
		world.setBlock(x - 1, y - 2, z, Blocks.flowing_water);
		world.setBlock(x + 1, y + 2, z + 1, ModBlocks.manaFlame);
		((TileManaFlame) world.getTileEntity(x + 1, y + 2, z + 1)).setColor(new Color(70 + world.rand.nextInt(185), 70 + world.rand.nextInt(185), 70 + world.rand.nextInt(185)).getRGB());

		int[][] rootPositions = new int[][] {
				{ -1, -3, -1 },
				{ -2, -4, -1 },
				{ -2, -4, -2 },
				{ +1, -4, -1 },
				{ +1, -5, -1 },
				{ +2, -5, -1 },
				{ +2, -6, +0 },
				{ +0, -4, +2 },
				{ +0, -5, +2 },
				{ +0, -5, +3 },
				{ +0, -6, +3 },
		};
		for(int[] root : rootPositions)
			world.setBlock(x + root[0], y + root[1], z + root[2], ModBlocks.root);

		world.setBlock(x, y - 4, z, Blocks.bedrock);
	}

}
