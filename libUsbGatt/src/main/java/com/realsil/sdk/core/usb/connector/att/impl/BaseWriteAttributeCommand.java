package com.realsil.sdk.core.usb.connector.att.impl;

/**
 * An abstract class template for creating Write Attribute PDUs Command
 * @author xp.chen
 */
abstract class BaseWriteAttributeCommand extends BaseAttributeProtocol {



    /**
     * Use this method to create a Write Attributes Command.
     */
    public abstract void createCommand();

}