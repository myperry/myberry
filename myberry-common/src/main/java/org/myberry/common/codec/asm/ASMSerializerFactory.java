/*
* MIT License
*
* Copyright (c) 2021 MyBerry. All rights reserved.
* https://myberry.org/
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:

*   * Redistributions of source code must retain the above copyright notice, this
* list of conditions and the following disclaimer.

*   * Redistributions in binary form must reproduce the above copyright notice,
* this list of conditions and the following disclaimer in the documentation
* and/or other materials provided with the distribution.

*   * Neither the name of MyBerry. nor the names of its contributors may be used
* to endorse or promote products derived from this software without specific
* prior written permission.

* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.

* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
*/
package org.myberry.common.codec.asm;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jdk.internal.org.objectweb.asm.ClassWriter;
import jdk.internal.org.objectweb.asm.FieldVisitor;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.Type;
import org.myberry.common.codec.MessageLite;
import org.myberry.common.codec.annotation.SerialField;
import org.myberry.common.codec.asm.serializer.MessageLiteSerializer;
import org.myberry.common.codec.exception.UnsupportedCodecTypeException;
import org.myberry.common.codec.formatter.InOutStream;
import org.myberry.common.codec.support.FieldType;
import org.myberry.common.codec.util.NullObjects;

public class ASMSerializerFactory implements Opcodes {

  private static final ASMClassLoader amsClassLoader = new ASMClassLoader();

  private static final Map<String /*ClassName*/, MessageLiteSerializer> messageLiteSerializerMap =
      new HashMap<>();

  private static final String ASM_CLASS_SERIALIZER_PRE = "ASMSerializer";
  private static final String ASM_DEFAULT_CONSTRUCTOR_NAME = "<init>";
  private static final String ASM_FINAL_FIELD_NAME_SERIALIZER_ADPTER = "asmSerializerAdpter";
  private static final String CLASS_MESSAGELITEERIALIZER_METHOD_NAME_WRITEMESSAGELITE =
      "writeMessageLite";
  private static final String CLASS_ASMSERIALIZERADPTER_METHOD_NAME_WRITETO = "writeTo";
  private static final String CLASS_FIELDTYPE_METHOD_NAME_ID = "id";
  private static final String CLASS_INOUTSTREAM_METHOD_NAME_PUTTAG = "putTag";
  private static final String CLASS_BOXEDTYPE_METHOD_NAME_VALUEOF = "valueOf";
  private static final String CLASS_COLLECTION_METHOD_NAME_SIZE = "size";
  private static final String CLASS_array_METHOD_NAME_SIZE = "length";

  private static final String CLASS_INOUTSTREAM_METHOD_PART_PUT = "put";
  private static final String CLASS_INOUTSTREAM_METHOD_PART_INT = "Int";
  private static final String CLASS_INOUTSTREAM_METHOD_PART_LONG = "Long";
  private static final String CLASS_INOUTSTREAM_METHOD_PART_FLOAT = "Float";
  private static final String CLASS_INOUTSTREAM_METHOD_PART_DOUBLE = "Double";
  private static final String CLASS_INOUTSTREAM_METHOD_PART_BOOLEAN = "Boolean";
  private static final String CLASS_INOUTSTREAM_METHOD_PART_STRING = "String";
  private static final String CLASS_INOUTSTREAM_METHOD_PART_LIST = "List";
  private static final String CLASS_INOUTSTREAM_METHOD_PART_SET = "Set";
  private static final String CLASS_INOUTSTREAM_METHOD_PART_ARRAY = "Array";
  private static final String CLASS_INOUTSTREAM_METHOD_PART_BOXEDARRAY = "BoxedArray";
  private static final String CLASS_INOUTSTREAM_METHOD_PART_LISTSIZE = "ListSize";
  private static final String CLASS_NULLOBJECTS_METHOD_GETDEFAULTINTIFABSENT =
      "getDefaultIntIfAbsent";
  private static final String CLASS_NULLOBJECTS_METHOD_GETDEFAULTLONGIFABSENT =
      "getDefaultLongIfAbsent";
  private static final String CLASS_NULLOBJECTS_METHOD_GETDEFAULTFLOATIFABSENT =
      "getDefaultFloatIfAbsent";
  private static final String CLASS_NULLOBJECTS_METHOD_GETDEFAULTDOUBLEIFABSENT =
      "getDefaultDoubleIfAbsent";
  private static final String CLASS_NULLOBJECTS_METHOD_GETDEFAULTBOOLEANIFABSENT =
      "getDefaultBooleanIfAbsent";
  private static final String CLASS_NULLOBJECTS_METHOD_GETDEFAULTSTRINGIFABSENT =
      "getDefaultStringIfAbsent";
  private static final String CLASS_NULLOBJECTS_METHOD_GETDEFAULTLISTIFABSENT =
      "getDefaultListIfAbsent";
  private static final String CLASS_NULLOBJECTS_METHOD_GETDEFAULTSETIFABSENT =
      "getDefaultSetIfAbsent";
  private static final String CLASS_NULLOBJECTS_METHOD_GETDEFAULTARRAYIFABSENT =
      "getDefaultArrayIfAbsent";

  private final ASMSerializerAdpter asmSerializerAdpter;

  public ASMSerializerFactory(final ASMSerializerAdpter asmSerializerAdpter) {
    this.asmSerializerAdpter = asmSerializerAdpter;
  }

  public MessageLiteSerializer getSerializer(Class<? extends MessageLite> clazz) throws Exception {
    MessageLiteSerializer messageLiteSerializer = messageLiteSerializerMap.get(clazz.getName());
    if (messageLiteSerializer != null) {
      return messageLiteSerializer;
    }

    synchronized (messageLiteSerializerMap) {
      messageLiteSerializer = messageLiteSerializerMap.get(clazz.getName());
      if (messageLiteSerializer != null) {
        return messageLiteSerializer;
      }

      messageLiteSerializer = factoryASMSerializer(clazz);
      messageLiteSerializerMap.put(clazz.getName(), messageLiteSerializer);
    }
    return messageLiteSerializer;
  }

  private MessageLiteSerializer factoryASMSerializer(Class<?> clazz) throws Exception {
    // define AMS class
    ClassWriter cw = new ClassWriter(0);

    ASMType asmClazzType =
        ASMUtils.generateASMClassTypeName(
            MessageLiteSerializer.class.getPackage().getName(),
            ASM_CLASS_SERIALIZER_PRE + clazz.getSimpleName());
    cw.visit(
        V1_5,
        ACC_PUBLIC + ACC_SUPER,
        asmClazzType.getNameType(),
        null,
        Type.getType(Object.class).getInternalName(),
        new String[] {Type.getType(MessageLiteSerializer.class).getInternalName()});

    // define final field
    FieldVisitor fieldVisitor =
        cw.visitField(
            ACC_PRIVATE + ACC_FINAL,
            ASM_FINAL_FIELD_NAME_SERIALIZER_ADPTER,
            Type.getType(ASMSerializerAdpter.class).getDescriptor(),
            null,
            null);
    fieldVisitor.visitEnd();

    // define default constructor
    MethodVisitor mv =
        cw.visitMethod(
            ACC_PUBLIC,
            ASM_DEFAULT_CONSTRUCTOR_NAME,
            ASMUtils.getMethodDescriptor(Type.getType(ASMSerializerAdpter.class).getDescriptor()),
            null,
            null);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(
        INVOKESPECIAL,
        Type.getType(Object.class).getInternalName(),
        ASM_DEFAULT_CONSTRUCTOR_NAME,
        ASMUtils.getMethodDescriptor(),
        false);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitVarInsn(ALOAD, 1);
    mv.visitFieldInsn(
        PUTFIELD,
        asmClazzType.getNameType(),
        ASM_FINAL_FIELD_NAME_SERIALIZER_ADPTER,
        Type.getType(ASMSerializerAdpter.class).getDescriptor());
    mv.visitInsn(RETURN);
    mv.visitMaxs(2, 2);
    mv.visitEnd();

    // define
    // org.myberry.common.codec.asm.serializer.MessageLiteSerializer.writeMessageLite(MessageLite,
    // InOutStream)
    mv =
        cw.visitMethod(
            ACC_PUBLIC,
            CLASS_MESSAGELITEERIALIZER_METHOD_NAME_WRITEMESSAGELITE,
            ASMUtils.getMethodDescriptor(
                Type.VOID_TYPE,
                Type.getType(MessageLite.class).getDescriptor(),
                Type.getType(InOutStream.class).getDescriptor()),
            null,
            new String[] {Type.getType(Exception.class).getInternalName()});
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 1);
    mv.visitTypeInsn(CHECKCAST, Type.getType(clazz).getInternalName());
    mv.visitVarInsn(ASTORE, 3);

    Field[] fields = clazz.getDeclaredFields();
    for (Field field : fields) {
      if (!Modifier.isStatic(field.getModifiers())
          && !Modifier.isFinal(field.getModifiers())
          && !Modifier.isTransient(field.getModifiers())
          && Modifier.isPrivate(field.getModifiers())) {

        SerialField annotation = field.getAnnotation(SerialField.class);
        if (null == annotation) {
          continue;
        } else if (annotation.ordinal() < 0) {
          throw new IllegalAccessException(
              "SerialField ordinal cannot be less than or equal to zero");
        }

        if (FieldType.INT.isValidForField(field)) {
          writeBaseTypeFormatField(
              mv,
              clazz,
              field,
              FieldType.INT,
              generateTypeMethodName(
                  CLASS_INOUTSTREAM_METHOD_PART_INT,
                  null,
                  CLASS_NULLOBJECTS_METHOD_GETDEFAULTINTIFABSENT));
        } else if (FieldType.LONG.isValidForField(field)) {
          writeBaseTypeFormatField(
              mv,
              clazz,
              field,
              FieldType.LONG,
              generateTypeMethodName(
                  CLASS_INOUTSTREAM_METHOD_PART_LONG,
                  null,
                  CLASS_NULLOBJECTS_METHOD_GETDEFAULTLONGIFABSENT));
        } else if (FieldType.FLOAT.isValidForField(field)) {
          writeBaseTypeFormatField(
              mv,
              clazz,
              field,
              FieldType.FLOAT,
              generateTypeMethodName(
                  CLASS_INOUTSTREAM_METHOD_PART_FLOAT,
                  null,
                  CLASS_NULLOBJECTS_METHOD_GETDEFAULTFLOATIFABSENT));
        } else if (FieldType.DOUBLE.isValidForField(field)) {
          writeBaseTypeFormatField(
              mv,
              clazz,
              field,
              FieldType.DOUBLE,
              generateTypeMethodName(
                  CLASS_INOUTSTREAM_METHOD_PART_DOUBLE,
                  null,
                  CLASS_NULLOBJECTS_METHOD_GETDEFAULTDOUBLEIFABSENT));
        } else if (FieldType.BOOLEAN.isValidForField(field)) {
          writeBaseTypeFormatField(
              mv,
              clazz,
              field,
              FieldType.BOOLEAN,
              generateTypeMethodName(
                  CLASS_INOUTSTREAM_METHOD_PART_BOOLEAN,
                  null,
                  CLASS_NULLOBJECTS_METHOD_GETDEFAULTBOOLEANIFABSENT));
        } else if (FieldType.STRING.isValidForField(field)) {
          writeBaseTypeFormatField(
              mv,
              clazz,
              field,
              FieldType.STRING,
              generateTypeMethodName(
                  CLASS_INOUTSTREAM_METHOD_PART_STRING,
                  null,
                  CLASS_NULLOBJECTS_METHOD_GETDEFAULTSTRINGIFABSENT));
        } else if (FieldType.MESSAGELITE.isValidForField(field)) {
          writeMessageLiteFormatField(
              asmClazzType.getNameType(), mv, clazz, field, FieldType.MESSAGELITE);
        } else if (FieldType.INT_PACKED.isValidForField(field)) {
          writeBaseTypeFormatField(
              mv,
              clazz,
              field,
              FieldType.INT_PACKED,
              generateTypeMethodName(
                  field, CLASS_INOUTSTREAM_METHOD_PART_INT, FieldType.INT_PACKED));
        } else if (FieldType.LONG_PACKED.isValidForField(field)) {
          writeBaseTypeFormatField(
              mv,
              clazz,
              field,
              FieldType.LONG_PACKED,
              generateTypeMethodName(
                  field, CLASS_INOUTSTREAM_METHOD_PART_LONG, FieldType.LONG_PACKED));
        } else if (FieldType.FLOAT_PACKED.isValidForField(field)) {
          writeBaseTypeFormatField(
              mv,
              clazz,
              field,
              FieldType.FLOAT_PACKED,
              generateTypeMethodName(
                  field, CLASS_INOUTSTREAM_METHOD_PART_FLOAT, FieldType.FLOAT_PACKED));
        } else if (FieldType.DOUBLE_PACKED.isValidForField(field)) {
          writeBaseTypeFormatField(
              mv,
              clazz,
              field,
              FieldType.DOUBLE_PACKED,
              generateTypeMethodName(
                  field, CLASS_INOUTSTREAM_METHOD_PART_DOUBLE, FieldType.DOUBLE_PACKED));
        } else if (FieldType.BOOLEAN_PACKED.isValidForField(field)) {
          writeBaseTypeFormatField(
              mv,
              clazz,
              field,
              FieldType.BOOLEAN_PACKED,
              generateTypeMethodName(
                  field, CLASS_INOUTSTREAM_METHOD_PART_BOOLEAN, FieldType.BOOLEAN_PACKED));
        } else if (FieldType.STRING_PACKED.isValidForField(field)) {
          writeBaseTypeFormatField(
              mv,
              clazz,
              field,
              FieldType.STRING_PACKED,
              generateTypeMethodName(
                  field, CLASS_INOUTSTREAM_METHOD_PART_STRING, FieldType.STRING_PACKED));
        } else if (FieldType.MESSAGELITE_PACKED.isValidForField(field)) {
          writeMessageLiteListFormatField(
              asmClazzType.getNameType(),
              mv,
              clazz,
              field,
              FieldType.MESSAGELITE_PACKED,
              generateTypeMethodName(
                  field, CLASS_INOUTSTREAM_METHOD_PART_LISTSIZE, FieldType.MESSAGELITE_PACKED));
        } else {
          throw new UnsupportedCodecTypeException("Unsupported field type: " + field.getType());
        }
      }
    }

    // end
    mv.visitInsn(RETURN);
    mv.visitMaxs(3, 4);
    mv.visitEnd();

    byte[] code = cw.toByteArray();

    Class<?> serializerClass =
        amsClassLoader.defineClassByASM(asmClazzType.getName(), code, 0, code.length);
    Constructor<?> constructor = serializerClass.getConstructor(ASMSerializerAdpter.class);
    Object instance = constructor.newInstance(asmSerializerAdpter);

    return (MessageLiteSerializer) instance;
  }

  private void writeBaseTypeFormatField(
      MethodVisitor mv,
      Class<?> clazz,
      Field field,
      FieldType fieldType,
      TypeMethodName typeMethodName) {
    mv.visitVarInsn(ALOAD, 2);
    writeTag(mv, field.getAnnotation(SerialField.class).ordinal(), fieldType.toString());
    mv.visitVarInsn(ALOAD, 3);
    writeValueOrValueList(mv, clazz, field, fieldType, typeMethodName);
    mv.visitInsn(POP);
  }

  private void writeMessageLiteFormatField(
      String nameType, MethodVisitor mv, Class<?> clazz, Field field, FieldType fieldType) {
    mv.visitVarInsn(ALOAD, 2);
    writeTag(mv, field.getAnnotation(SerialField.class).ordinal(), fieldType.toString());
    mv.visitInsn(POP);

    mv.visitVarInsn(ALOAD, 0);
    mv.visitFieldInsn(
        GETFIELD,
        nameType,
        ASM_FINAL_FIELD_NAME_SERIALIZER_ADPTER,
        Type.getType(ASMSerializerAdpter.class).getDescriptor());
    mv.visitVarInsn(ALOAD, 3);
    mv.visitMethodInsn(
        INVOKEVIRTUAL,
        Type.getType(clazz).getInternalName(),
        ASMUtils.getFieldGetterMethodName(field),
        ASMUtils.getMethodDescriptor(Type.getType(field.getType()), null),
        false);
    mv.visitVarInsn(ALOAD, 2);
    mv.visitMethodInsn(
        INVOKEVIRTUAL,
        Type.getType(ASMSerializerAdpter.class).getInternalName(),
        CLASS_ASMSERIALIZERADPTER_METHOD_NAME_WRITETO,
        ASMUtils.getMethodDescriptor(
            Type.getType(MessageLite.class).getDescriptor(),
            Type.getType(InOutStream.class).getDescriptor()),
        false);
  }

  private void writeMessageLiteListFormatField(
      String nameType,
      MethodVisitor mv,
      Class<?> clazz,
      Field field,
      FieldType fieldType,
      TypeMethodName typeMethodName) {
    mv.visitVarInsn(ALOAD, 2);
    writeTag(mv, field.getAnnotation(SerialField.class).ordinal(), fieldType.toString());
    mv.visitVarInsn(ALOAD, 3);
    writeMessageLiteListSize(mv, clazz, field, typeMethodName);
    mv.visitInsn(POP);

    mv.visitVarInsn(ALOAD, 0);
    mv.visitFieldInsn(
        GETFIELD,
        nameType,
        ASM_FINAL_FIELD_NAME_SERIALIZER_ADPTER,
        Type.getType(ASMSerializerAdpter.class).getDescriptor());
    mv.visitVarInsn(ALOAD, 3);
    mv.visitMethodInsn(
        INVOKEVIRTUAL,
        Type.getType(clazz).getInternalName(),
        ASMUtils.getFieldGetterMethodName(field),
        ASMUtils.getMethodDescriptor(Type.getType(field.getType()), null),
        false);
    mv.visitVarInsn(ALOAD, 2);
    mv.visitMethodInsn(
        INVOKEVIRTUAL,
        Type.getType(ASMSerializerAdpter.class).getInternalName(),
        CLASS_ASMSERIALIZERADPTER_METHOD_NAME_WRITETO,
        ASMUtils.getMethodDescriptor(
            Type.getType(typeMethodName.getPackedType()).getDescriptor(),
            Type.getType(InOutStream.class).getDescriptor()),
        false);
  }

  private void writeTag(MethodVisitor mv, int ordinal, String fieldTypeName) {
    mv.visitLdcInsn(ordinal);
    mv.visitFieldInsn(
        GETSTATIC,
        Type.getType(FieldType.class).getInternalName(),
        fieldTypeName,
        Type.getType(FieldType.class).getDescriptor());
    mv.visitMethodInsn(
        INVOKEVIRTUAL,
        Type.getType(FieldType.class).getInternalName(),
        CLASS_FIELDTYPE_METHOD_NAME_ID,
        ASMUtils.getMethodDescriptor(Type.getType(int.class), null),
        false);
    mv.visitMethodInsn(
        INVOKEVIRTUAL,
        Type.getType(InOutStream.class).getInternalName(),
        CLASS_INOUTSTREAM_METHOD_NAME_PUTTAG,
        ASMUtils.getMethodDescriptor(
            Type.getType(InOutStream.class),
            Type.getType(int.class).getDescriptor(),
            Type.getType(int.class).getDescriptor()),
        false);
  }

  private void writeValueOrValueList(
      MethodVisitor mv,
      Class<?> clazz,
      Field field,
      FieldType fieldType,
      TypeMethodName typeMethodName) {
    if (!fieldType.isPacked()) {
      writeValue(mv, clazz, field, fieldType, typeMethodName);
    } else {
      writeValueList(mv, clazz, field, typeMethodName);
    }
  }

  private void writeValue(
      MethodVisitor mv,
      Class<?> clazz,
      Field field,
      FieldType fieldType,
      TypeMethodName typeMethodName) {
    mv.visitMethodInsn(
        INVOKEVIRTUAL,
        Type.getType(clazz).getInternalName(),
        ASMUtils.getFieldGetterMethodName(field),
        ASMUtils.getMethodDescriptor(
            Type.getType(
                field.getType() != fieldType.getBoxedType()
                    ? fieldType.getType()
                    : fieldType.getBoxedType()),
            null),
        false);
    if (field.getType() != fieldType.getBoxedType()
        && fieldType.isDifferentBetweenTypeAndBoxedType()) {
      mv.visitMethodInsn(
          INVOKESTATIC,
          Type.getType(fieldType.getBoxedType()).getInternalName(),
          CLASS_BOXEDTYPE_METHOD_NAME_VALUEOF,
          ASMUtils.getMethodDescriptor(
              Type.getType(fieldType.getBoxedType()),
              Type.getType(fieldType.getType()).getDescriptor()),
          false);
    }
    mv.visitMethodInsn(
        INVOKESTATIC,
        Type.getType(NullObjects.class).getInternalName(),
        typeMethodName.getDefaultValueName(),
        ASMUtils.getMethodDescriptor(
            Type.getType(fieldType.getType()),
            Type.getType(fieldType.getBoxedType()).getDescriptor()),
        false);
    mv.visitMethodInsn(
        INVOKEVIRTUAL,
        Type.getType(InOutStream.class).getInternalName(),
        typeMethodName.getPutName(),
        ASMUtils.getMethodDescriptor(
            Type.getType(InOutStream.class), Type.getType(fieldType.getType()).getDescriptor()),
        false);
  }

  private void writeValueList(
      MethodVisitor mv, Class<?> clazz, Field field, TypeMethodName typeMethodName) {
    mv.visitMethodInsn(
        INVOKEVIRTUAL,
        Type.getType(clazz).getInternalName(),
        ASMUtils.getFieldGetterMethodName(field),
        ASMUtils.getMethodDescriptor(Type.getType(typeMethodName.getPackedType()), null),
        false);
    mv.visitMethodInsn(
        INVOKESTATIC,
        Type.getType(NullObjects.class).getInternalName(),
        typeMethodName.getDefaultValueName(),
        ASMUtils.getMethodDescriptor(
            Type.getType(typeMethodName.getPackedType()),
            Type.getType(typeMethodName.getPackedType()).getDescriptor()),
        false);
    mv.visitMethodInsn(
        INVOKEVIRTUAL,
        Type.getType(InOutStream.class).getInternalName(),
        typeMethodName.getPutName(),
        ASMUtils.getMethodDescriptor(
            Type.getType(InOutStream.class),
            Type.getType(typeMethodName.getPackedType()).getDescriptor()),
        false);
  }

  private void writeMessageLiteListSize(
      MethodVisitor mv, Class<?> clazz, Field field, TypeMethodName typeMethodName) {
    mv.visitMethodInsn(
        INVOKEVIRTUAL,
        Type.getType(clazz).getInternalName(),
        ASMUtils.getFieldGetterMethodName(field),
        ASMUtils.getMethodDescriptor(Type.getType(field.getType())),
        false);
    mv.visitMethodInsn(
        INVOKESTATIC,
        Type.getType(NullObjects.class).getInternalName(),
        typeMethodName.getDefaultValueName(),
        ASMUtils.getMethodDescriptor(
            Type.getType(typeMethodName.getPackedType()),
            Type.getType(typeMethodName.getPackedType()).getDescriptor()),
        false);
    if (field.getType().isArray()) {
      mv.visitInsn(ARRAYLENGTH);
    } else {
      mv.visitMethodInsn(
          INVOKEINTERFACE,
          Type.getType(typeMethodName.getPackedType()).getInternalName(),
          CLASS_COLLECTION_METHOD_NAME_SIZE,
          ASMUtils.getMethodDescriptor(Type.getType(int.class), null),
          true);
    }
    mv.visitMethodInsn(
        INVOKEVIRTUAL,
        Type.getType(InOutStream.class).getInternalName(),
        typeMethodName.getPutName(),
        ASMUtils.getMethodDescriptor(
            Type.getType(InOutStream.class), Type.getType(int.class).getDescriptor()),
        false);
  }

  private static TypeMethodName generateTypeMethodName(
      String type, String packed, String defaultValueName) {
    StringBuilder builder = new StringBuilder();
    builder.append(CLASS_INOUTSTREAM_METHOD_PART_PUT);
    builder.append(type);
    if (null != packed) {
      builder.append(packed);
    }

    return new TypeMethodName(builder.toString(), defaultValueName);
  }

  private static TypeMethodName generateTypeMethodName(
      Field field, String type, FieldType fieldType) {
    Class<?> clazz = field.getType();
    if (List.class.isAssignableFrom(clazz)) {
      if (FieldType.MESSAGELITE_PACKED == fieldType) {
        TypeMethodName typeMethodName =
            generateTypeMethodName(
                CLASS_INOUTSTREAM_METHOD_PART_LISTSIZE,
                null,
                CLASS_NULLOBJECTS_METHOD_GETDEFAULTLISTIFABSENT);
        typeMethodName.setPackedType(clazz);
        return typeMethodName;
      } else {
        TypeMethodName typeMethodName =
            generateTypeMethodName(
                type,
                CLASS_INOUTSTREAM_METHOD_PART_LIST,
                CLASS_NULLOBJECTS_METHOD_GETDEFAULTLISTIFABSENT);
        typeMethodName.setPackedType(List.class);
        return typeMethodName;
      }
    } else if (Set.class.isAssignableFrom(clazz)) {
      if (FieldType.MESSAGELITE_PACKED == fieldType) {
        TypeMethodName typeMethodName =
            generateTypeMethodName(
                CLASS_INOUTSTREAM_METHOD_PART_LISTSIZE,
                null,
                CLASS_NULLOBJECTS_METHOD_GETDEFAULTSETIFABSENT);
        typeMethodName.setPackedType(clazz);
        return typeMethodName;
      } else {
        TypeMethodName typeMethodName =
            generateTypeMethodName(
                type,
                CLASS_INOUTSTREAM_METHOD_PART_SET,
                CLASS_NULLOBJECTS_METHOD_GETDEFAULTSETIFABSENT);
        typeMethodName.setPackedType(Set.class);
        return typeMethodName;
      }
    } else if (clazz.isArray()) {
      if (FieldType.INT.getType() == clazz.getComponentType()
          || FieldType.LONG.getType() == clazz.getComponentType()
          || FieldType.FLOAT.getType() == clazz.getComponentType()
          || FieldType.DOUBLE.getType() == clazz.getComponentType()
          || FieldType.BOOLEAN.getType() == clazz.getComponentType()
          || FieldType.STRING.getType() == clazz.getComponentType()) {
        TypeMethodName typeMethodName =
            generateTypeMethodName(
                type,
                CLASS_INOUTSTREAM_METHOD_PART_ARRAY,
                CLASS_NULLOBJECTS_METHOD_GETDEFAULTARRAYIFABSENT);
        typeMethodName.setPackedType(clazz);
        return typeMethodName;
      } else if (FieldType.INT.getBoxedType() == clazz.getComponentType()
          || FieldType.LONG.getBoxedType() == clazz.getComponentType()
          || FieldType.FLOAT.getBoxedType() == clazz.getComponentType()
          || FieldType.DOUBLE.getBoxedType() == clazz.getComponentType()
          || FieldType.BOOLEAN.getBoxedType() == clazz.getComponentType()
          || FieldType.STRING.getBoxedType() == clazz.getComponentType()) {
        TypeMethodName typeMethodName =
            generateTypeMethodName(
                type,
                CLASS_INOUTSTREAM_METHOD_PART_BOXEDARRAY,
                CLASS_NULLOBJECTS_METHOD_GETDEFAULTARRAYIFABSENT);
        typeMethodName.setPackedType(clazz);
        return typeMethodName;
      } else if (FieldType.MESSAGELITE_PACKED == fieldType) {
        TypeMethodName typeMethodName =
            generateTypeMethodName(
                CLASS_INOUTSTREAM_METHOD_PART_LISTSIZE,
                null,
                CLASS_NULLOBJECTS_METHOD_GETDEFAULTARRAYIFABSENT);
        typeMethodName.setPackedType(MessageLite[].class);
        return typeMethodName;
      }
    }

    throw new UnsupportedCodecTypeException(clazz.getName());
  }

  private static class TypeMethodName {
    private final String putName;
    private final String defaultValueName;

    private Class<?> packedType;

    public TypeMethodName(String putName, String defaultValueName) {
      this.putName = putName;
      this.defaultValueName = defaultValueName;
    }

    public String getPutName() {
      return putName;
    }

    public String getDefaultValueName() {
      return defaultValueName;
    }

    public Class<?> getPackedType() {
      return packedType;
    }

    public void setPackedType(Class<?> packedType) {
      this.packedType = packedType;
    }
  }
}
