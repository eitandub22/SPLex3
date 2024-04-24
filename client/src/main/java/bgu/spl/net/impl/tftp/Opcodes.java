package bgu.spl.net.impl.tftp;

public enum Opcodes {
    READ(1), WRITE(2), DATA(3), ACK(4), ERROR(5), DIR(6), LOGIN(7), DELETE(8), CAST(9), DISCONNECT(10);
    private final int value;

    Opcodes(int value) {
        this.value = value;
    }

    public static Opcodes getOpcode(int value) {
        for (Opcodes opcode : Opcodes.values()) {
            if (opcode.getValue() == value) {
                return opcode;
            }
        }
        return null;
    }

    public int getValue() {
        return value;
    }

    public boolean exists(int val){
        for(Opcodes opcodes: values()){
            if(opcodes.getValue() == val) return true;
        }
        return false;
    }
}
