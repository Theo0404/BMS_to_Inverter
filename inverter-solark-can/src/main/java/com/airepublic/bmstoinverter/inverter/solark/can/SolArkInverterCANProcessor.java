package com.airepublic.bmstoinverter.inverter.solark.can;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import com.airepublic.bmstoinverter.core.AlarmLevel;
import com.airepublic.bmstoinverter.core.Inverter;
import com.airepublic.bmstoinverter.core.Port;
import com.airepublic.bmstoinverter.core.bms.data.Alarm;
import com.airepublic.bmstoinverter.core.bms.data.BatteryPack;
import com.airepublic.bmstoinverter.core.protocol.can.CANPort;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * The class to handle CAN messages for SolArk {@link Inverter}.
 */
@ApplicationScoped
public class SolArkInverterCANProcessor extends Inverter {

    @Override
    protected List<ByteBuffer> createSendFrames(final ByteBuffer requestFrame, final BatteryPack aggregatedPack) {
        final List<ByteBuffer> frames = new ArrayList<>();

        frames.add(createChargeDischargeInfo(aggregatedPack)); // 0x351
        frames.add(createSOC(aggregatedPack)); // 0x355
        frames.add(createBatteryVoltage(aggregatedPack)); // 0x356
        frames.add(createManufacturer(aggregatedPack)); // 0x35E
        frames.add(createAlarms(aggregatedPack)); // 0x359

        return frames;
    }


    @Override
    protected ByteBuffer readRequest(final Port port) throws IOException {
        return null;
    }


    @Override
    protected void sendFrame(final Port port, final ByteBuffer frame) throws IOException {
        ((CANPort) port).sendExtendedFrame(frame);
    }


    // 0x351
    private ByteBuffer createChargeDischargeInfo(final BatteryPack pack) {
        final ByteBuffer frame = prepareFrame(0x351);

        // Battery charge voltage (0.1V) - u_int_16
        frame.putChar((char) pack.maxPackVoltageLimit);
        // Charge current limit (0.1A) - s_int_16
        frame.putShort((short) pack.maxPackChargeCurrent);
        // Discharge current limit (0.1A) - s_int_16
        frame.putShort((short) pack.maxPackDischargeCurrent);
        // Battery discharge voltage (0.1V) - u_int_16
        frame.putChar((char) pack.minPackVoltageLimit);

        return frame;

    }


    // 0x355
    private ByteBuffer createSOC(final BatteryPack pack) {
        final ByteBuffer frame = prepareFrame(0x355);

        // SOC (1%) - u_int_16
        frame.putChar((char) (pack.packSOC / 10));
        // SOH (1%) - u_int_16
        frame.putChar((char) (pack.packSOH / 10));

        return frame;
    }


    // 0x356
    private ByteBuffer createBatteryVoltage(final BatteryPack pack) {
        final ByteBuffer frame = prepareFrame(0x356);

        // Battery voltage (0.01V) - u_int_16
        frame.putShort((short) (pack.packVoltage * 10));
        // Battery current (0.1A) - u_int_16
        frame.putShort((short) pack.packCurrent);
        // Battery temperature (0.1C) - u_int_16
        frame.putShort((short) pack.tempAverage);

        return frame;
    }


    // 0x35E
    private ByteBuffer createManufacturer(final BatteryPack pack) {
        final ByteBuffer frame = prepareFrame(0x35E);
        int idx = 0;

        while (idx < pack.manufacturerCode.length() && idx < 8) {
            frame.putChar(pack.manufacturerCode.charAt(idx));
            idx++;
        }

        return frame;
    }


    // 0x359
    private ByteBuffer createAlarms(final BatteryPack pack) {
        final BitSet bits = new BitSet(32);
        final ByteBuffer frame = prepareFrame(0x359);

        // protection alarms
        bits.set(1, pack.getAlarmLevel(Alarm.CELL_VOLTAGE_HIGH) == AlarmLevel.ALARM);
        bits.set(2, pack.getAlarmLevel(Alarm.CELL_VOLTAGE_LOW) == AlarmLevel.ALARM);
        bits.set(3, pack.getAlarmLevel(Alarm.CELL_TEMPERATURE_HIGH) == AlarmLevel.ALARM);
        bits.set(4, pack.getAlarmLevel(Alarm.CELL_TEMPERATURE_LOW) == AlarmLevel.ALARM);
        bits.set(7, pack.getAlarmLevel(Alarm.DISCHARGE_CURRENT_HIGH) == AlarmLevel.ALARM);
        bits.set(8, pack.getAlarmLevel(Alarm.CHARGE_CURRENT_HIGH) == AlarmLevel.ALARM);
        bits.set(11, pack.getAlarmLevel(Alarm.FAILURE_OTHER) == AlarmLevel.ALARM);

        // warning alarms
        bits.set(17, pack.getAlarmLevel(Alarm.CELL_VOLTAGE_HIGH) == AlarmLevel.WARNING);
        bits.set(18, pack.getAlarmLevel(Alarm.CELL_VOLTAGE_LOW) == AlarmLevel.WARNING);
        bits.set(19, pack.getAlarmLevel(Alarm.CELL_TEMPERATURE_HIGH) == AlarmLevel.WARNING);
        bits.set(20, pack.getAlarmLevel(Alarm.CELL_TEMPERATURE_LOW) == AlarmLevel.WARNING);
        bits.set(23, pack.getAlarmLevel(Alarm.DISCHARGE_CURRENT_HIGH) == AlarmLevel.WARNING);
        bits.set(24, pack.getAlarmLevel(Alarm.CHARGE_CURRENT_HIGH) == AlarmLevel.WARNING);
        bits.set(11, pack.getAlarmLevel(Alarm.FAILURE_OTHER) == AlarmLevel.WARNING);

        return frame;
    }


    private ByteBuffer prepareFrame(final int cmd) {
        final ByteBuffer frame = ByteBuffer.allocateDirect(16).order(ByteOrder.LITTLE_ENDIAN);
        frame.putInt(cmd)
                .put((byte) 8)
                .put((byte) 0) // flags
                .putShort((short) 0); // skip 2 bytes
        return frame;
    }


    public static void main(final String[] args) {
        final BatteryPack pack = new BatteryPack();
        pack.packVoltage = 535;
        pack.packCurrent = 15;
        pack.packSOC = 94;
        pack.packSOH = 100;
        pack.tempMax = 220;

        final SolArkInverterCANProcessor processor = new SolArkInverterCANProcessor();
        final ByteBuffer frame = processor.createSOC(pack);

        System.out.println(Port.printBuffer(frame));
    }

}
