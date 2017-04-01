//THIS IS A GENERATED CLASS FILE
package com.builtbroken.mc.codegen.tests;

import com.builtbroken.mc.api.tile.ITankProvider;
import com.builtbroken.mc.framework.logic.annotations.TankProviderWrapped;
import com.builtbroken.mc.framework.logic.wrapper.TileEntityWrapper;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.*;

public class TileEntityWrapperTestTank extends TileEntityWrapper implements IFluidHandler
{
	public TileEntityWrapperTestTank()
	{
		super(new TileFluidTank());
	}

	//============================
	//==Methods:TankProviderWrapped
	//============================


    protected IFluidTank getFluidTank(ForgeDirection from, Fluid fluid)
    {
        if (getTileNode() instanceof ITankProvider)
        {
            return ((ITankProvider) getTileNode()).getTankForFluid(from, fluid);
        }
        return null;
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
    {
        if (resource != null)
        {
            IFluidTank tank = getFluidTank(from, resource.getFluid());
            if (tank != null)
            {
                return tank.fill(resource, doFill);
            }
        }
        return 0;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
    {
        if (resource != null)
        {
            IFluidTank tank = getFluidTank(from, resource.getFluid());
            if (tank != null && tank.getFluid() != null && tank.getFluid().getFluid() == resource.getFluid())
            {
                return tank.drain(resource.amount, doDrain);
            }
        }
        return null;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
    {
        IFluidTank tank = getFluidTank(from, null);
        if (tank != null && tank.getFluid() != null)
        {
            return tank.drain(maxDrain, doDrain);
        }
        return null;
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid)
    {
        if (getTileNode() instanceof ITankProvider)
        {
            return ((ITankProvider) getTileNode()).canFill(from, fluid);
        }
        return false;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid)
    {
        if (getTileNode() instanceof ITankProvider)
        {
            return ((ITankProvider) getTileNode()).canDrain(from, fluid);
        }
        return false;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from)
    {
        return new FluidTankInfo[0];
    }
    
}