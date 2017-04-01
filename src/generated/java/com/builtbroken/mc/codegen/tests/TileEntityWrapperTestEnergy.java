//THIS IS A GENERATED CLASS FILE
package com.builtbroken.mc.codegen.tests;

import com.builtbroken.mc.api.energy.IEnergyBuffer;
import com.builtbroken.mc.api.energy.IEnergyBufferProvider;
import com.builtbroken.mc.framework.logic.wrapper.TileEntityWrapper;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEntityWrapperTestEnergy extends TileEntityWrapper implements IEnergyBufferProvider
{
	public TileEntityWrapperTestEnergy()
	{
		super(new TileTestEnergy());
	}

	//============================
	//==Methods:EnergyWrapped
	//============================


    @Override
    public IEnergyBuffer getEnergyBuffer(ForgeDirection side)
    {
        if (getTileNode() instanceof IEnergyBufferProvider)
        {
            return ((IEnergyBufferProvider) getTileNode()).getEnergyBuffer(side);
        }
        return null;
    }
    
}