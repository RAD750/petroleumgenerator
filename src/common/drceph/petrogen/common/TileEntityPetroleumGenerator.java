package drceph.petrogen.common;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import buildcraft.BuildCraftEnergy;

//import ic2.api.Direction;
//import ic2.api.EnergyNet;
//import ic2.api.IEnergySource;
//import ic2.api.INetworkDataProvider;
//import ic2.api.INetworkUpdateListener;
//import ic2.api.NetworkHelper;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagInt;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.liquids.ILiquidTank;
import net.minecraftforge.liquids.ITankContainer;
import net.minecraftforge.liquids.LiquidContainerRegistry;
import net.minecraftforge.liquids.LiquidDictionary;
import net.minecraftforge.liquids.LiquidStack;
import net.minecraftforge.liquids.LiquidTank;

/**
 * @author chris 
 *         fuel outputs 96000EU @ 16EU/t; oil outputs 9600EU @ 8EU/t
 *         for easy maths:
 *         100000EU @ 20EU/t (1 'volume' == 5 ticks); oil 10000EU @ 10EU/t (1 'volume' == 1 tick)
 */
public class TileEntityPetroleumGenerator extends TileEntity implements
		 IInventory, ITankContainer {

	private ItemStack[] inventory;
	public static final int SLOT_COUNT = 1;
	private boolean initialised;
	public static final int MAX_VOLUME = LiquidContainerRegistry.BUCKET_VOLUME * 10;
	public static final int SCALE = 58;
	public int amount;
	public int liquidId;
	public int liquidMeta;
	public int charge;
	public static final int[] fuelIds = {LiquidContainerRegistry.getLiquidForFilledItem(new ItemStack(BuildCraftEnergy.bucketOil)).itemID,};
	

	public TileEntityPetroleumGenerator() {
		this.inventory = new ItemStack[SLOT_COUNT];
		liquidId = 0;
		liquidMeta = 0;
		amount = 0;
		charge = 0;
	}
	
	/**
	 * gets the current liquidstack and liquidstack volume for this tank
	 * @return LiquidStack representing the tank contents, null if empty tank.
	 */
	public LiquidStack getLiquidStack() {
		if (liquidId != 0) {
			return new LiquidStack(liquidId,amount,liquidMeta);
		} else {
			return null;
		}
	}

	@Override
	public void updateEntity() {

		boolean changed = false;
		// Add to energynet on first update, and initialise network voodoo
		if (!initialised && worldObj != null) {
			if (worldObj.isRemote) {
				//NetworkHelper.requestInitialData(this);
			} else {
				//EnergyNet.getForWorld(worldObj).addTileEntity(this);
			}
			initialised = true;
		}
		
		//only runs it on the serverside, so that it updates the client???
		if (!this.worldObj.isRemote) {
			
			if (this.inventory[0] != null && this.inventory[0].getItem().hasContainerItem()) {
				System.err.println("Contains: "+LiquidContainerRegistry.getLiquidForFilledItem(this.inventory[0]).asItemStack().itemID);
			}
			
			if (this.inventory[0] != null
					&& this.inventory[0].getItem().equals(Item.coal)) {
//				if (timer == 0) {
//					--this.inventory[0].stackSize;
//					if (this.inventory[0].stackSize == 0) {
//						this.inventory[0] = null;
//					}
//					timer = 20;
//					changed = true;
//				} else {
//					timer--;
//				}
				
				/* this returns the amount of power unused - use this to define how much fuel is used.
				*  maybe fuel will only be unused if full power emission is not used?
				*  
				*  i.e. if output packet is 16, but 12 is unused, use up full tick worth of fuel.
				*      however if output packet is 16 and 16 is unused, do not use fuel (not emitting)??
				*  
				*/
//				int unused = EnergyNet.getForWorld(worldObj).emitEnergyFrom(this,this.getMaxEnergyOutput());
//				
//				if (timer == 20) {
//					System.out.println("unused power: "+unused);
//				}
			}
			
		}
		
		if (changed) {
			onInventoryChanged();
		}

	}

//	@Override
//	public boolean emitsEnergyTo(TileEntity receiver, Direction direction) {
//		return true;
//	}

	// @Override
	public boolean isAddedToEnergyNet() {
		return initialised;
	}

	@Override
	public void invalidate() {
		// remove from EnergyNet when invalidating the TE
		if (worldObj != null && initialised) {
			//EnergyNet.getForWorld(worldObj).removeTileEntity(this);
		}
		super.invalidate();
	}

	//@Override
	public int getMaxEnergyOutput() {
		return 16;
	}

	@Override
	public int getSizeInventory() {
		// TODO Auto-generated method stub
		return inventory.length;
	}
	
	@Override
	public ItemStack getStackInSlot(int i) {
		return inventory[i];
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		if (inventory[i] != null) {
			if (inventory[i].stackSize <= j) {
				ItemStack itemstack = inventory[i];
				inventory[i] = null;
				onInventoryChanged();
				return itemstack;
			}
			ItemStack itemstack1 = inventory[i].splitStack(j);
			if (inventory[i].stackSize == 0) {
				inventory[i] = null;
			}
			onInventoryChanged();
			return itemstack1;
		} else {
			return null;
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1) {
		if (this.inventory[var1] != null) {
			ItemStack var2 = this.inventory[var1];
			this.inventory[var1] = null;
			return var2;
		} else {
			return null;
		}
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		inventory[i] = itemstack;
		if (itemstack != null && itemstack.stackSize > getInventoryStackLimit()) {
			itemstack.stackSize = getInventoryStackLimit();
		}
		onInventoryChanged();
	}

	@Override
	public String getInvName() {
		return "TileEntityPetroleumGenerator";
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityPlayer) {
		if (worldObj == null) {
			return true;
		}
		if (worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) != this) {
			return false;
		}
		return entityPlayer.getDistanceSq((double) xCoord + 0.5D,
				(double) yCoord + 0.5D, (double) zCoord + 0.5D) <= 64D;
	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);

		NBTTagList tagList = tagCompound.getTagList("Inventory");

		for (int i = 0; i < tagList.tagCount(); i++) {
			NBTTagCompound tag = (NBTTagCompound) tagList.tagAt(i);

			byte slot = tag.getByte("Slot");

			if (slot >= 0 && slot < inventory.length) {
				inventory[slot] = ItemStack.loadItemStackFromNBT(tag);
			}
		}
		liquidId = tagCompound.getInteger("liquidId");
		liquidMeta = tagCompound.getInteger("liquidMeta");
		amount = tagCompound.getInteger("amount");
		charge = tagCompound.getInteger("charge");
	}

	@Override
	public void writeToNBT(NBTTagCompound tagCompound) {
		super.writeToNBT(tagCompound);

		NBTTagList itemList = new NBTTagList();

		for (int i = 0; i < inventory.length; i++) {
			ItemStack stack = inventory[i];

			if (stack != null) {
				NBTTagCompound tag = new NBTTagCompound();

				tag.setByte("Slot", (byte) i);
				stack.writeToNBT(tag);
				itemList.appendTag(tag);
			}
		}

		tagCompound.setTag("Inventory", itemList);
		tagCompound.setInteger("liquidId", liquidId);
		tagCompound.setInteger("liquidMeta", liquidMeta);
		tagCompound.setInteger("amount", amount);
		tagCompound.setInteger("charge",charge);
	}

	@Override
	public void openChest() {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeChest() {
		// TODO Auto-generated method stub

	}

	//@Override
	public void onNetworkUpdate(String field) {

	}

	private static List<String> fields = Arrays.asList(new String[0]);

	//@Override
	public List<String> getNetworkedFields() {
		return fields;
	}
	
	public int getScaledVolume() {
		double ratio = MAX_VOLUME / (SCALE*1.0);
		double scaled_volume = amount / ratio;
		return ((int) Math.round(scaled_volume));
	}
	
	@Override
	public int fill(ForgeDirection from, LiquidStack resource, boolean doFill) {
		return this.fill(0, resource, doFill);
	}

	@Override
	public int fill(int tankIndex, LiquidStack resource, boolean doFill) {
		//System.err.println("Current: "+liquidId+":"+liquidMeta+" Input: "+resource.itemID+":"+resource.itemMeta);
		if (liquidId == 0) {
			liquidId = resource.itemID;
			liquidMeta = resource.itemMeta;
		}
		
		if (liquidId == resource.itemID && liquidMeta != resource.itemMeta) {
			return 0;
		} else if (liquidId != resource.itemID) {
			return 0;
		}
		
		int payload = resource.amount;
		
		if (amount+payload <= MAX_VOLUME) {
			if (doFill) {
				amount += payload;
			}
			return payload;
		} else {
			int difference = MAX_VOLUME - amount;
			if (doFill) {
				amount = MAX_VOLUME;
			}
			return difference;
		}
	}

	@Override
	public LiquidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LiquidStack drain(int tankIndex, int maxDrain, boolean doDrain) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ILiquidTank[] getTanks(ForgeDirection direction) {
		return new LiquidTank[] {new LiquidTank(MAX_VOLUME)};
	}

	@Override
	public ILiquidTank getTank(ForgeDirection direction, LiquidStack type) {
		return null;
	}

}
