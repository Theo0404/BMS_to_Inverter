/**
 * This software is free to use and to distribute in its unchanged form for private use.
 * Commercial use is prohibited without an explicit license agreement of the copyright holder.
 * Any changes to this software must be made solely in the project repository at https://github.com/ai-republic/bms-to-inverter.
 * The copyright holder is not liable for any damages in whatever form that may occur by using this software.
 *
 * (c) Copyright 2022 and onwards - Torsten Oltmanns
 *
 * @author Torsten Oltmanns - bms-to-inverter''AT''gmail.com
 */
package com.airepublic.bmstoinverter.inverter.pylon.can;

import com.airepublic.bmstoinverter.core.Inverter;
import com.airepublic.bmstoinverter.core.InverterConfig;
import com.airepublic.bmstoinverter.core.InverterDescriptor;
import com.airepublic.bmstoinverter.core.Port;
import com.airepublic.bmstoinverter.protocol.can.JavaCANPort;

/**
 * The {@link InverterDescriptor} for the Pylontech {@link Inverter} using the CAN protocol.
 */
public class PylonInverterCANDescriptor implements InverterDescriptor {
    @Override
    public String getName() {
        return "PYLON_CAN";
    }


    @Override
    public Class<? extends Inverter> getInverterClass() {
        return PylonInverterCANProcessor.class;
    }


    @Override
    public Port createPort(final InverterConfig config) {
        final Port port = new JavaCANPort(config.getPortLocator(), 500000);
        return port;
    }

}
