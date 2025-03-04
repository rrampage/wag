package rrampage.wasp;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import rrampage.wasp.data.*;
import rrampage.wasp.instructions.*;
import rrampage.wasp.vm.Machine;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static org.junit.jupiter.api.Assertions.*;
import static rrampage.wasp.utils.ConversionUtils.*;

public class MachineTest {
    private static final int MEM_PAGES = 1;
    private static final Object[] TEST_OBJ_ARR = new Object[] {1111, -111111L};

    private static final Function addFunction = new Function("add",
            new FunctionType(new ValueType.NumType[]{ValueType.NumType.I32, ValueType.NumType.I32}, new ValueType.NumType[]{ValueType.NumType.I32}), null,
            new Instruction[] {
                    new FunctionInstruction.LocalGet(0),
                    new FunctionInstruction.LocalGet(1),
                    IntBinaryInstruction.I32_ADD
            });

    @Test
    public void shouldPushConst() {
        Instruction[] startIns = new Instruction[]{
                new ConstInstruction.FloatConst(2.0f),
                new ConstInstruction.DoubleConst(1.0),
                new ConstInstruction.IntConst(4),
                new ConstInstruction.LongConst(1112345345667L),
        };

        Machine m = Machine.createAndStart(new Function[]{Function.createStartFunction("pushConst", startIns)}, null, null, MEM_PAGES, null, null, 0);
        assertEquals(1112345345667L, m.pop());
        assertEquals(4, m.popInt());
        assertEquals(1.0, m.popDouble(), 0.0);
        assertEquals(2.0f, m.popFloat(), 0.0f);
    }

    @Test
    public void shouldDrop() {
        Instruction[] ins = new Instruction[]{
                new ConstInstruction.IntConst(4),
                new ConstInstruction.LongConst(1112345345667L),
                UnaryInstruction.DROP,
        };
        Machine m = Machine.createAndStart(new Function[]{Function.createStartFunction("shouldDrop", ins)}, null, null, MEM_PAGES, null, null, 0);
        assertEquals(4, m.popInt());
    }

    @Test
    public void shouldAdd() {
        double l = 123.0;
        double r = 345.0;
        float fl = 223.5f;
        float fr = -223.5f;
        long ll = 1234567890;
        long lr = 1111111111;
        int il = 11111;
        int ir = 1111111;
        Instruction[] ins = new Instruction[]{
                new ConstInstruction.DoubleConst(l),
                new ConstInstruction.DoubleConst(r),
                DoubleBinaryInstruction.F64_ADD,
                new ConstInstruction.FloatConst(fl),
                new ConstInstruction.FloatConst(fr),
                FloatBinaryInstruction.F32_ADD,
                new ConstInstruction.IntConst(il),
                new ConstInstruction.IntConst(ir),
                IntBinaryInstruction.I32_ADD,
                new ConstInstruction.LongConst(ll),
                new ConstInstruction.LongConst(lr),
                LongBinaryInstruction.I64_ADD,
        };
        Machine m = Machine.createAndStart(new Function[]{Function.createStartFunction("shouldAdd", ins)}, null, null, MEM_PAGES, null, null, 0);
        assertEquals(ll+lr, m.pop());
        assertEquals(il+ir, m.popInt());
        assertEquals(0.0f, m.popFloat(), 0.0f);
        assertEquals(m.popDouble(), l + r, 0.0);
    }

    @Test
    public void shouldAddAndCompare() {
        double l = 123.0;
        double r = 345.0;
        double a = l+r;
        float fl = 223.5f;
        float fr = -223.5f;
        float fa = fl + fr;
        long ll = 1234567890;
        long lr = 1111111111;
        long la = ll + lr;
        int il = 11111;
        int ir = 1111111;
        int ia = il + ir;
        Instruction[] ins = new Instruction[]{
                new ConstInstruction.DoubleConst(l),
                new ConstInstruction.DoubleConst(r),
                DoubleBinaryInstruction.F64_ADD,
                new ConstInstruction.DoubleConst(a),
                DoubleBinaryInstruction.F64_EQ,
                new ConstInstruction.FloatConst(fl),
                new ConstInstruction.FloatConst(fr),
                FloatBinaryInstruction.F32_ADD,
                new ConstInstruction.FloatConst(fa),
                FloatBinaryInstruction.F32_EQ,
                new ConstInstruction.IntConst(il),
                new ConstInstruction.IntConst(ir),
                IntBinaryInstruction.I32_ADD,
                new ConstInstruction.IntConst(ia),
                IntBinaryInstruction.I32_EQ,
                new ConstInstruction.LongConst(ll),
                new ConstInstruction.LongConst(lr),
                LongBinaryInstruction.I64_ADD,
                new ConstInstruction.LongConst(la),
                LongBinaryInstruction.I64_EQ,
        };
        Machine m = Machine.createAndStart(new Function[]{Function.createStartFunction("shouldAddAndCompare", ins)}, null, null, MEM_PAGES, null, null, 0);
        assertEquals(m.popInt(), 1);
        assertEquals(m.popInt(), 1);
        assertEquals(m.popInt(), 1);
        assertEquals(m.popInt(), 1);
    }

    @Test
    public void shouldStoreAndLoad() {
        int iAddr = 8;
        int fAddr = iAddr+4;
        int lAddr = fAddr+8;
        int dAddr = lAddr+8;
        int sAddr = dAddr+8;
        int i = 1111, s = 100000;
        long l = -1111111111L;
        float f = 111111.0f;
        double d = 1111.0;
        Instruction[] ins = new Instruction[]{
                new ConstInstruction.IntConst(dAddr),
                new ConstInstruction.DoubleConst(d),
                new ConstInstruction.IntConst(lAddr),
                new ConstInstruction.LongConst(l),
                new ConstInstruction.IntConst(fAddr),
                new ConstInstruction.FloatConst(f),
                new ConstInstruction.IntConst(iAddr),
                new ConstInstruction.IntConst(i),
                new ConstInstruction.IntConst(sAddr),
                new ConstInstruction.IntConst(s),
                new StoreInstruction.I32Store16(1, 0),
                new StoreInstruction.I32Store(0, 0),
                new StoreInstruction.F32Store(0, 0),
                new StoreInstruction.I64Store(0, 0),
                new StoreInstruction.F64Store(0, 0),
                new ConstInstruction.IntConst(dAddr),
                new LoadInstruction.F64Load(0, 0),
                new ConstInstruction.IntConst(lAddr),
                new LoadInstruction.I64Load(0, 0),
                new ConstInstruction.IntConst(fAddr),
                new LoadInstruction.F32Load(0, 0),
                new ConstInstruction.IntConst(iAddr),
                new LoadInstruction.I32Load(0,0),
                new ConstInstruction.IntConst(sAddr),
                new LoadInstruction.I32Load16U(0, 0),
        };
        Machine m = Machine.createAndStart(new Function[]{Function.createStartFunction("shouldStoreAndLoad", ins)}, null, null, MEM_PAGES, null, null, 0);
        assertEquals(m.popInt(), s%65536);
        assertEquals(m.popInt(), i);
        assertEquals(m.popFloat(), f, 0.0f);
        assertEquals(m.pop(), l);
        assertEquals(m.popDouble(), d, 0.0);
        long x = Short.toUnsignedLong(bytesToShort(m.getMainMemory().load(sAddr, 2)));
        assertEquals(x, s%65536);
    }

    @Test
    public void shouldCallIntConstFunction() {
        int i = 42;
        Function fun = new Function("const", new FunctionType(null, new ValueType.NumType[]{ValueType.NumType.I32}), null, new Instruction[]{new ConstInstruction.IntConst(i)});
        Instruction[] ins = new Instruction[] {
          new FunctionInstruction.Call(0)
        };
        Machine m = Machine.createAndStart(new Function[]{fun, Function.createStartFunction("shouldCallIntConst", ins)}, null, null, MEM_PAGES, null, null, 1);
        assertEquals(m.popInt(), i);
    }

    @Test
    public void shouldCallAddIntFunction() {
        int a = 123, b = -123;
        Instruction[] ins = new Instruction[] {
                new ConstInstruction.IntConst(b),
                new ConstInstruction.IntConst(a),
                new FunctionInstruction.Call(0),
                new ConstInstruction.IntConst(b),
                new ConstInstruction.IntConst(a),
                IntBinaryInstruction.I32_ADD,
                IntBinaryInstruction.I32_EQ
        };
        Machine m = Machine.createAndStart(new Function[]{addFunction, Function.createStartFunction("shouldAdd", ins)}, null, null, MEM_PAGES, null, null, 1);
        assertEquals(m.popInt(), 1);
    }

    @Test
    public void shouldLoopUsingGlobal() {
        Variable[] globals = new Variable[]{Variable.newMutableVariable(ValueType.NumType.I32, 0)};
        Instruction[] loopIns = new Instruction[] {
                new GlobalInstruction.GlobalGet(0),
                new ConstInstruction.IntConst(1),
                IntBinaryInstruction.I32_ADD,
                new GlobalInstruction.GlobalSet(0),
                new GlobalInstruction.GlobalGet(0),
                new ConstInstruction.IntConst(10),
                IntBinaryInstruction.I32_LT_S,
                new ControlFlowInstruction.BranchIf(0),
        };
        Instruction[] ins = new Instruction[] {
                new ControlFlowInstruction.Loop(0, null, loopIns),
                new GlobalInstruction.GlobalGet(0),
        };
        Machine m = Machine.createAndStart(new Function[]{Function.createStartFunction("shouldLoopGlobal", ins)}, null, globals, MEM_PAGES, null, null, 0);
        assertEquals(10, m.popInt());
    }

    @Test
    public void shouldLoopFunction() {
        Instruction[] loopIns = new Instruction[] {
                new FunctionInstruction.LocalGet(0),
                new ConstInstruction.IntConst(1),
                IntBinaryInstruction.I32_ADD,
                new FunctionInstruction.LocalTee(0),
                new ConstInstruction.IntConst(10),
                IntBinaryInstruction.I32_LT_S,
                new ControlFlowInstruction.BranchIf(1),
        };
        Instruction[] funIns = new Instruction[] {
                new ConstInstruction.IntConst(0),
                new ControlFlowInstruction.Loop(1, null, loopIns),
                new FunctionInstruction.LocalGet(0),
                new ConstInstruction.IntConst(10),
                IntBinaryInstruction.I32_EQ,
                new ControlFlowInstruction.IfElse(2, FunctionType.VOID, Instruction.of(UnaryInstruction.DROP), Instruction.of(NullaryInstruction.UNREACHABLE))
        };
        Function f = new Function("loop_check", new FunctionType(null, null),
                new ValueType.NumType[]{ValueType.NumType.I32}, funIns);
        Instruction[] ins = new Instruction[] {
                new FunctionInstruction.Call(0),
        };
        Machine m = Machine.createAndStart(new Function[]{f, Function.createStartFunction("shouldLoop", ins)}, null, null, MEM_PAGES, null, null, 1);
        System.out.println(m.stackView());
        assertEquals("", m.stackView());
    }

    @Test
    public void shouldReturnValidImportFunction() {
        FunctionType type = new FunctionType(new ValueType.NumType[]{ValueType.NumType.F64, ValueType.NumType.F64}, new ValueType.NumType[]{ValueType.NumType.F64});
        MethodType mt = MethodType.methodType(double.class, double.class, double.class);
        MethodHandle mh;
        try {
            mh = MethodHandles.lookup().findStatic(Math.class, "pow", mt);
        } catch (Exception e) {
            Assertions.assertNull(e); // placeholder to fail
            return;
        }
        Function fun = Function.createImportFunction("pow", type, mh);
        Assertions.assertNotNull(fun);
    }

    @Test
    public void shouldCallJavaMethod() {
        double a = 4.0, b = 2.0;
        double res = Math.pow(a, b);
        FunctionType type = new FunctionType(new ValueType.NumType[]{ValueType.NumType.F64, ValueType.NumType.F64}, new ValueType.NumType[]{ValueType.NumType.F64});
        MethodType mt = FunctionType.getMethodTypeFromFunctionType(type);
        MethodHandle mh;
        try {
            mh = MethodHandles.lookup().findStatic(Math.class, "pow", mt);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            Assertions.assertNull(e); // placeholder to fail
            return;
        }
        Instruction[] funIns = new Instruction[]{
                new FunctionInstruction.CallJava(type, mh),
        };
        Function fun = new Function("pow", type, null, funIns);
        Instruction[] ins = new Instruction[]{
                new ConstInstruction.DoubleConst(b),
                new ConstInstruction.DoubleConst(a),
                new FunctionInstruction.Call(0)
        };
        Machine m = Machine.createAndStart(new Function[]{fun, Function.createStartFunction("shouldCallJava", ins)}, null, null, MEM_PAGES, null, null, 1);
        double callRes = m.popDouble();
        assertEquals(callRes, res, 0.0);
    }

    @Test
    public void shouldCallJavaMethodWithHomogenousMultipleReturn() {
        double a = 4.0, b = 2.0;
        double c = a *2, d = b*2;
        FunctionType type = new FunctionType(new ValueType.NumType[]{ValueType.NumType.F64, ValueType.NumType.F64}, new ValueType.NumType[]{ValueType.NumType.F64, ValueType.NumType.F64});
        MethodType mt = FunctionType.getMethodTypeFromFunctionType(type);
        assertEquals(MethodType.methodType(double[].class, double.class, double.class), mt);
        MethodHandle mh;
        try {
            mh = MethodHandles.lookup().findStatic(MachineTest.class, "doubleArr", mt);
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.assertNull(e); // placeholder to fail
            return;
        }
        Function fun = Function.createImportFunction("doubleArr", type, mh);
        Instruction[] ins = new Instruction[]{
                new ConstInstruction.DoubleConst(b),
                new ConstInstruction.DoubleConst(a),
                new FunctionInstruction.Call(0)
        };
        Machine m = Machine.createAndStart(new Function[]{fun, Function.createStartFunction("shouldCallJava", ins)}, null, null, MEM_PAGES, null, null, 1);
        double callRes2 = m.popDouble();
        double callRes1 = m.popDouble();
        assertEquals(callRes1, c, 0.0);
        assertEquals(callRes2, d, 0.0);
    }

    @Test
    public void shouldCallJavaMethodWithHeterogenousMultipleReturn() {
        FunctionType type = new FunctionType(new ValueType.NumType[]{}, new ValueType.NumType[]{ValueType.NumType.I32, ValueType.NumType.I64});
        MethodType mt = FunctionType.getMethodTypeFromFunctionType(type);
        MethodHandle mh;
        try {
            mh = MethodHandles.lookup().findStatic(MachineTest.class, "objArr", MethodType.methodType(Object[].class));
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.assertNull(e); // placeholder to fail
            return;
        }
        Function fun = Function.createImportFunction("objArr", type, mh);
        Instruction[] ins = new Instruction[]{
                new FunctionInstruction.Call(0)
        };
        Machine m = Machine.createAndStart(new Function[]{fun, Function.createStartFunction("shouldCallJava", ins)}, null, null, MEM_PAGES, null, null, 1);
        int callRes1 = m.popInt();
        long callRes2 = m.pop();
        assertEquals(callRes1, TEST_OBJ_ARR[0]);
        assertEquals(callRes2, TEST_OBJ_ARR[1]);
    }

    @Test
    public void shouldCallIndirectAddIntFunction() {
        int a = 123, b = -123;
        int tblOffset = 3;
        Table t = new Table(4);
        t.set(tblOffset, addFunction);
        Instruction[] ins = new Instruction[] {
                new ConstInstruction.IntConst(b),
                new ConstInstruction.IntConst(a),
                new ConstInstruction.IntConst(tblOffset),
                new FunctionInstruction.CallIndirect(0, (byte) 0),
                new ConstInstruction.IntConst(b),
                new ConstInstruction.IntConst(a),
                IntBinaryInstruction.I32_ADD,
                IntBinaryInstruction.I32_EQ
        };
        Machine m = Machine.createAndStart(new Function[]{addFunction, Function.createStartFunction("shouldCallIndirect", ins)}, new Table[]{t}, null, MEM_PAGES, null, null, 1);
        assertEquals(m.popInt(), 1);
    }

    @Test
    public void shouldSignExtend8I32() {
        // From https://bugzilla.mozilla.org/attachment.cgi?id=8960898&action=diff#a/js/src/jit-test/tests/wasm/spec/src/extend32.wast_sec2
        Instruction[] ins = new Instruction[]{
                new ConstInstruction.IntConst(0x7f),
                UnaryInstruction.I32_EXTEND8_S,
                new ConstInstruction.IntConst(0x80),
                UnaryInstruction.I32_EXTEND8_S,
                new ConstInstruction.IntConst(0xff),
                UnaryInstruction.I32_EXTEND8_S,
                new ConstInstruction.IntConst(0x012345_00),
                UnaryInstruction.I32_EXTEND8_S,
                new ConstInstruction.IntConst(0xfedcba_80),
                UnaryInstruction.I32_EXTEND8_S,
                new ConstInstruction.IntConst(-1),
                UnaryInstruction.I32_EXTEND8_S,
        };
        Machine m = Machine.createAndStart(new Function[]{Function.createStartFunction("shouldSignExtend8I32", ins)}, null, null, MEM_PAGES, null, null, 0);
        assertEquals(m.popInt(), -1);
        assertEquals(m.popInt(), -0x80);
        assertEquals(m.popInt(), 0);
        assertEquals(m.popInt(), -1);
        assertEquals(m.popInt(), -128);
        assertEquals(m.popInt(), 0x7f);
    }

    @Test
    public void shouldSignExtend16I32() {
        // From https://bugzilla.mozilla.org/attachment.cgi?id=8960898&action=diff#a/js/src/jit-test/tests/wasm/spec/src/extend32.wast_sec2
        Instruction[] ins = new Instruction[]{
                new ConstInstruction.IntConst(0x7fff),
                UnaryInstruction.I32_EXTEND16_S,
                new ConstInstruction.IntConst(0x8000),
                UnaryInstruction.I32_EXTEND16_S,
                new ConstInstruction.IntConst(0xffff),
                UnaryInstruction.I32_EXTEND16_S,
                new ConstInstruction.IntConst(0x0123_0000),
                UnaryInstruction.I32_EXTEND16_S,
                new ConstInstruction.IntConst(0xfedc_8000),
                UnaryInstruction.I32_EXTEND16_S,
                new ConstInstruction.IntConst(-1),
                UnaryInstruction.I32_EXTEND16_S,
        };
        Machine m = Machine.createAndStart(new Function[]{Function.createStartFunction("shouldSignExtend16I32", ins)}, null, null, MEM_PAGES, null, null, 0);
        assertEquals(-1, m.popInt());
        assertEquals(-0x8000, m.popInt());
        assertEquals(0, m.popInt());
        assertEquals(-1, m.popInt());
        assertEquals(Short.MIN_VALUE, m.popInt());
        assertEquals(Short.MAX_VALUE, m.popInt());
    }

    private static Object[] objArr() {
        return TEST_OBJ_ARR;
    }

    private static double[] doubleArr(double a, double b) {
        return new double[] {a*2, b*2};
    }
}
