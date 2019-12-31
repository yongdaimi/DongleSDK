package com.realsil.sdk.core.usb.connector.att.impl;

import com.realsil.sdk.core.usb.connector.att.AttributeOpcodeDefine;
import com.realsil.sdk.core.usb.connector.att.AttributeParseResult;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * The Read By Group Type Request is used to obtain the values of attributes where the attribute type
 * is known, the type of a grouping attribute as defined by a higher layer specification, but the handle
 * is not known.
 *
 * @author xp.chen
 */
public class ReadByGroupTypeRequest extends BaseReadByGroupTypeRequest {

    /**
     * First requested handle number
     */
    private short mStartingAttHandle;

    /**
     * ast requested handle number
     */
    private short mEndingAttHandle;

    /**
     * 16 octet UUID
     */
    private int mAttGroupTypeIn16;

    /**
     * 2 octet UUID
     */
    private short mAttGroupTypeIn2;

    /**
     * Default starting attribute handle
     */
    private static final int DEFAULT_START_ATT_HANDLE = 0X0001;

    /**
     * Default ending attribute handle
     */
    private static final int DEFAULT_END_ATT_HANDLE = 0xFFFF;


    /**
     * Use this constructor to create a Read By Group Type Request
     * <p>Note: The difference from the {@link ReadByGroupTypeRequest#ReadByGroupTypeRequest(int, int, short)} method is
     * it can be used to search all attributes between start handle is 0x0001 and end handle is 0xFFFF.
     * </p>
     * <p>
     * attGroupType is defined in {@link com.realsil.sdk.core.usb.connector.att.AttributeTypeDefine}
     * </p>
     *
     * @param attGroupType 2 or 16 octet UUID
     * @see com.realsil.sdk.core.usb.connector.att.AttributeTypeDefine
     */
    public ReadByGroupTypeRequest(short attGroupType) {
        this(DEFAULT_START_ATT_HANDLE, DEFAULT_END_ATT_HANDLE, attGroupType);
    }


    /**
     * Use this constructor to create a Read By Group Type Request.
     *
     * <p>
     * attGroupType is defined in {@link com.realsil.sdk.core.usb.connector.att.AttributeTypeDefine}
     * </p>
     *
     * @param startingAttHandle First requested handle number
     * @param endingAttHandle   Last requested handle number
     * @param attGroupType      2 or 16 octet UUID
     * @see com.realsil.sdk.core.usb.connector.att.AttributeTypeDefine
     */
    public ReadByGroupTypeRequest(int startingAttHandle, int endingAttHandle, short attGroupType) {
        this.mStartingAttHandle = (short) startingAttHandle;
        this.mEndingAttHandle = (short) endingAttHandle;
        this.mAttGroupTypeIn2 = attGroupType;
    }


    @Override
    public void setRequestOpcode() {
        this.request_opcode = AttributeOpcodeDefine.READ_BY_GROUP_TYPE_REQUEST;
    }

    @Override
    public void createRequest() {
        super.createRequest();

        ByteBuffer byteBuffer = ByteBuffer.wrap(mSendData);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        /// Put Protocol Header
        // ReportID
        byteBuffer.put(mReportID);
        // message length(ATT PDU length)
        byteBuffer.put(1, (byte) mAttPduLength);

        /// Put Att PDU
        // Att opcode
        byteBuffer.put(2, request_opcode);
        // starting handle
        byteBuffer.putShort(3, mStartingAttHandle);
        // ending handle
        byteBuffer.putShort(5, mEndingAttHandle);
        // attribute group type
        byteBuffer.putShort(7, mAttGroupTypeIn2);
    }


    @Override
    public void parseResponse(byte[] response) {
        super.parseResponse(response);
        if (response_opcode == AttributeOpcodeDefine.READ_BY_GROUP_TYPE_RESPONSE) {
            byte attribute_data_length = response[1];
            int attribute_data_list_length = response.length - 2; // Attribute Opcode(1B) + Length(1B) + Attribute Data List(4 to (ATT_MTU- 2))
            byte[] attribute_data_list = new byte[attribute_data_list_length];
            System.arraycopy(response, 2, attribute_data_list, 0, attribute_data_list_length);

            if (getReadByGroupTypeRequestCallback() != null) {
                getReadByGroupTypeRequestCallback().onReadSuccess(attribute_data_length & 0x0FF, attribute_data_list);
            }
            mParseResult = AttributeParseResult.PARSE_SUCCESS;
        } else {
            if (getReadByGroupTypeRequestCallback() != null) {
                getReadByGroupTypeRequestCallback().onReceiveFailed(response_opcode, error_request_opcode, error_att_handle, error_code);
            }
        }
    }

}