/**
 * CeriTalkSOAPStub.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.tektronix.www.cerify.soap.client;

import javax.annotation.Generated;

@Generated(value = {"axis"})public class CeriTalkSOAPStub extends org.apache.axis.client.Stub implements com.tektronix.www.cerify.soap.client.CeriTalk_PortType {
    private java.util.Vector cachedSerClasses = new java.util.Vector();
    private java.util.Vector cachedSerQNames = new java.util.Vector();
    private java.util.Vector cachedSerFactories = new java.util.Vector();
    private java.util.Vector cachedDeserFactories = new java.util.Vector();

    static org.apache.axis.description.OperationDesc [] _operations;

    static {
        _operations = new org.apache.axis.description.OperationDesc[25];
        _initOperationDesc1();
        _initOperationDesc2();
        _initOperationDesc3();
    }

    private static void _initOperationDesc1(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetMediaFileResults");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.tek.com/cerify/2010/12/20/reports/wsdl", "GetMediaFileResults"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.tek.com/cerify/2010/12/20/reports/wsdl", ">GetMediaFileResults"), com.tektronix.www.cerify.soap.client._20101220.GetMediaFileResults.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetMediaFileResultsResponse"));
        oper.setReturnClass(com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetMediaFileResultsResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GeneralFault"),
                      "com.tektronix.www.cerify.soap.client.BaseCeritalkFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "BaseCeritalkFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetMediaFileResultsJobDoesntExistFault"),
                      "com.tektronix.www.cerify.soap.client.JobDoesntExistFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "JobDoesntExistFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetMediaFileResultsMediaFileNotInJobFault"),
                      "com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "MediaFileNotInJobFault"), 
                      true
                     ));
        _operations[0] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetMediaFileResults_Deprecated");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetMediaFileResults"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetMediaFileResults"), com.tektronix.www.cerify.soap.client.GetMediaFileResults.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetMediaFileResultsResponse"));
        oper.setReturnClass(com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetMediaFileResultsResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GeneralFault"),
                      "com.tektronix.www.cerify.soap.client.BaseCeritalkFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "BaseCeritalkFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetMediaFileResultsJobDoesntExistFault"),
                      "com.tektronix.www.cerify.soap.client.JobDoesntExistFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "JobDoesntExistFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetMediaFileResultsMediaFileNotInJobFault"),
                      "com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "MediaFileNotInJobFault"), 
                      true
                     ));
        _operations[1] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetMediaFileResults_Deprecated_Dec09");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.tek.com/cerify/2008/05/09/reports/wsdl", "GetMediaFileResults"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.tek.com/cerify/2008/05/09/reports/wsdl", ">GetMediaFileResults"), com.tektronix.www.cerify.soap.client._20080509.GetMediaFileResults.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetMediaFileResultsResponse"));
        oper.setReturnClass(com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetMediaFileResultsResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GeneralFault"),
                      "com.tektronix.www.cerify.soap.client.BaseCeritalkFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "BaseCeritalkFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetMediaFileResultsJobDoesntExistFault"),
                      "com.tektronix.www.cerify.soap.client.JobDoesntExistFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "JobDoesntExistFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetMediaFileResultsMediaFileNotInJobFault"),
                      "com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "MediaFileNotInJobFault"), 
                      true
                     ));
        _operations[2] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetMediaFileResults_Deprecated_May10");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.tek.com/cerify/2009/12/02/reports/wsdl", "GetMediaFileResults"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.tek.com/cerify/2009/12/02/reports/wsdl", ">GetMediaFileResults"), com.tektronix.www.cerify.soap.client._20091202.GetMediaFileResults.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetMediaFileResultsResponse"));
        oper.setReturnClass(com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetMediaFileResultsResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GeneralFault"),
                      "com.tektronix.www.cerify.soap.client.BaseCeritalkFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "BaseCeritalkFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetMediaFileResultsJobDoesntExistFault"),
                      "com.tektronix.www.cerify.soap.client.JobDoesntExistFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "JobDoesntExistFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetMediaFileResultsMediaFileNotInJobFault"),
                      "com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "MediaFileNotInJobFault"), 
                      true
                     ));
        _operations[3] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetMediaFileResults_Deprecated_Dec10");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.tek.com/cerify/2010/05/23/reports/wsdl", "GetMediaFileResults"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.tek.com/cerify/2010/05/23/reports/wsdl", ">GetMediaFileResults"), com.tektronix.www.cerify.soap.client._20100523.GetMediaFileResults.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetMediaFileResultsResponse"));
        oper.setReturnClass(com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetMediaFileResultsResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GeneralFault"),
                      "com.tektronix.www.cerify.soap.client.BaseCeritalkFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "BaseCeritalkFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetMediaFileResultsJobDoesntExistFault"),
                      "com.tektronix.www.cerify.soap.client.JobDoesntExistFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "JobDoesntExistFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetMediaFileResultsMediaFileNotInJobFault"),
                      "com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "MediaFileNotInJobFault"), 
                      true
                     ));
        _operations[4] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("CreateMediaSet");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "CreateMediaSet"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">CreateMediaSet"), com.tektronix.www.cerify.soap.client.CreateMediaSet.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">createMediaSetResponse"));
        oper.setReturnClass(com.tektronix.www.cerify.soap.client.CreateMediaSetResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "createMediaSetResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "CreateMediaSetURLNotInMediaLocationFault"),
                      "com.tektronix.www.cerify.soap.client.URLNotInMediaLocationFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "URLNotInMediaLocationFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "CreateMediaSetMediaLocationDoesntExistFault"),
                      "com.tektronix.www.cerify.soap.client.MediaLocationDoesntExistFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "MediaLocationDoesntExistFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GeneralFault"),
                      "com.tektronix.www.cerify.soap.client.BaseCeritalkFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "BaseCeritalkFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "CreateMediaSetMediaSetNameInUseFault"),
                      "com.tektronix.www.cerify.soap.client.MediaSetNameInUseFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "MediaSetNameInUseFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "CreateMediaSetURLNotAccessibleFault"),
                      "com.tektronix.www.cerify.soap.client.URLNotAccessibleFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "URLNotAccessibleFault"), 
                      true
                     ));
        _operations[5] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("CreateJob");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "CreateJob"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">CreateJob"), com.tektronix.www.cerify.soap.client.CreateJob.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">createJobResponse"));
        oper.setReturnClass(com.tektronix.www.cerify.soap.client.CreateJobResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "createJobResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "CreateJobJobNameInUseFault"),
                      "com.tektronix.www.cerify.soap.client.JobNameInUseFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "JobNameInUseFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GeneralFault"),
                      "com.tektronix.www.cerify.soap.client.BaseCeritalkFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "BaseCeritalkFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "CreateJobMediaSetDoesntExistFault"),
                      "com.tektronix.www.cerify.soap.client.MediaSetDoesntExistFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "MediaSetDoesntExistFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "CreateJobProfileDoesntExistFault"),
                      "com.tektronix.www.cerify.soap.client.ProfileDoesntExistFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "ProfileDoesntExistFault"), 
                      true
                     ));
        _operations[6] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetMediaFileStatus");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetMediaFileStatus"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetMediaFileStatus"), com.tektronix.www.cerify.soap.client.GetMediaFileStatus.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetMediaFileStatusResponse"));
        oper.setReturnClass(com.tektronix.www.cerify.soap.client.GetMediaFileStatusResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetMediaFileStatusResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GeneralFault"),
                      "com.tektronix.www.cerify.soap.client.BaseCeritalkFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "BaseCeritalkFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetMediaFileStatusJobDoesntExistFault"),
                      "com.tektronix.www.cerify.soap.client.JobDoesntExistFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "JobDoesntExistFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetMediaFileStatusMediaFileNotInJobFault"),
                      "com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "MediaFileNotInJobFault"), 
                      true
                     ));
        _operations[7] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetJobStatus");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetJobStatus"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetJobStatus"), com.tektronix.www.cerify.soap.client.GetJobStatus.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetJobStatusResponse"));
        oper.setReturnClass(com.tektronix.www.cerify.soap.client.GetJobStatusResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetJobStatusResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GeneralFault"),
                      "com.tektronix.www.cerify.soap.client.BaseCeritalkFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "BaseCeritalkFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetJobStatusJobDoesntExistFault"),
                      "com.tektronix.www.cerify.soap.client.JobDoesntExistFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "JobDoesntExistFault"), 
                      true
                     ));
        _operations[8] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetJobResults");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.tek.com/cerify/2010/12/20/reports/wsdl", "GetJobResults"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.tek.com/cerify/2010/12/20/reports/wsdl", ">GetJobResults"), com.tektronix.www.cerify.soap.client._20101220.GetJobResults.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetJobResultsResponse"));
        oper.setReturnClass(com.tektronix.www.cerify.soap.client.GetJobResultsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetJobResultsResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GeneralFault"),
                      "com.tektronix.www.cerify.soap.client.BaseCeritalkFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "BaseCeritalkFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetJobResultsJobDoesntExistFault"),
                      "com.tektronix.www.cerify.soap.client.JobDoesntExistFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "JobDoesntExistFault"), 
                      true
                     ));
        _operations[9] = oper;

    }

    private static void _initOperationDesc2(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetJobResults_Deprecated");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetJobResults"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetJobResults"), com.tektronix.www.cerify.soap.client.GetJobResults.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetJobResultsResponse"));
        oper.setReturnClass(com.tektronix.www.cerify.soap.client.GetJobResultsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetJobResultsResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GeneralFault"),
                      "com.tektronix.www.cerify.soap.client.BaseCeritalkFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "BaseCeritalkFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetJobResultsJobDoesntExistFault"),
                      "com.tektronix.www.cerify.soap.client.JobDoesntExistFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "JobDoesntExistFault"), 
                      true
                     ));
        _operations[10] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetJobResults_Deprecated_Dec09");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.tek.com/cerify/2008/05/09/reports/wsdl", "GetJobResults"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.tek.com/cerify/2008/05/09/reports/wsdl", ">GetJobResults"), com.tektronix.www.cerify.soap.client._20080509.GetJobResults.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetJobResultsResponse"));
        oper.setReturnClass(com.tektronix.www.cerify.soap.client.GetJobResultsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetJobResultsResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GeneralFault"),
                      "com.tektronix.www.cerify.soap.client.BaseCeritalkFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "BaseCeritalkFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetJobResultsJobDoesntExistFault"),
                      "com.tektronix.www.cerify.soap.client.JobDoesntExistFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "JobDoesntExistFault"), 
                      true
                     ));
        _operations[11] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetJobResults_Deprecated_May10");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.tek.com/cerify/2009/12/02/reports/wsdl", "GetJobResults"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.tek.com/cerify/2009/12/02/reports/wsdl", ">GetJobResults"), com.tektronix.www.cerify.soap.client._20091202.GetJobResults.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetJobResultsResponse"));
        oper.setReturnClass(com.tektronix.www.cerify.soap.client.GetJobResultsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetJobResultsResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GeneralFault"),
                      "com.tektronix.www.cerify.soap.client.BaseCeritalkFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "BaseCeritalkFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetJobResultsJobDoesntExistFault"),
                      "com.tektronix.www.cerify.soap.client.JobDoesntExistFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "JobDoesntExistFault"), 
                      true
                     ));
        _operations[12] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetJobResults_Deprecated_Dec10");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.tek.com/cerify/2010/05/23/reports/wsdl", "GetJobResults"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.tek.com/cerify/2010/05/23/reports/wsdl", ">GetJobResults"), com.tektronix.www.cerify.soap.client._20100523.GetJobResults.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetJobResultsResponse"));
        oper.setReturnClass(com.tektronix.www.cerify.soap.client.GetJobResultsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetJobResultsResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GeneralFault"),
                      "com.tektronix.www.cerify.soap.client.BaseCeritalkFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "BaseCeritalkFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetJobResultsJobDoesntExistFault"),
                      "com.tektronix.www.cerify.soap.client.JobDoesntExistFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "JobDoesntExistFault"), 
                      true
                     ));
        _operations[13] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetMediaResultsFromAllRuns");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.tek.com/cerify/2010/12/20/reports/wsdl", "GetMediaResultsFromAllRuns"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.tek.com/cerify/2010/12/20/reports/wsdl", ">GetMediaResultsFromAllRuns"), com.tektronix.www.cerify.soap.client._20101220.GetMediaResultsFromAllRuns.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2009/12/02/reports/wsdl", ">GetMediaResultsFromAllRunsResponse"));
        oper.setReturnClass(com.tektronix.www.cerify.soap.client._20091202.GetMediaResultsFromAllRunsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.tek.com/cerify/2009/12/02/reports/wsdl", "GetMediaResultsFromAllRunsResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GeneralFault"),
                      "com.tektronix.www.cerify.soap.client.BaseCeritalkFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "BaseCeritalkFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetMediaFileResultsJobDoesntExistFault"),
                      "com.tektronix.www.cerify.soap.client.JobDoesntExistFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "JobDoesntExistFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetMediaFileResultsMediaFileNotInJobFault"),
                      "com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "MediaFileNotInJobFault"), 
                      true
                     ));
        _operations[14] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetMediaResultsFromAllRuns_Deprecated_Dec10");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.tek.com/cerify/2010/05/23/reports/wsdl", "GetMediaResultsFromAllRuns"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.tek.com/cerify/2010/05/23/reports/wsdl", ">GetMediaResultsFromAllRuns"), com.tektronix.www.cerify.soap.client._20100523.GetMediaResultsFromAllRuns.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2009/12/02/reports/wsdl", ">GetMediaResultsFromAllRunsResponse"));
        oper.setReturnClass(com.tektronix.www.cerify.soap.client._20091202.GetMediaResultsFromAllRunsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.tek.com/cerify/2009/12/02/reports/wsdl", "GetMediaResultsFromAllRunsResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GeneralFault"),
                      "com.tektronix.www.cerify.soap.client.BaseCeritalkFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "BaseCeritalkFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetMediaFileResultsJobDoesntExistFault"),
                      "com.tektronix.www.cerify.soap.client.JobDoesntExistFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "JobDoesntExistFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetMediaFileResultsMediaFileNotInJobFault"),
                      "com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "MediaFileNotInJobFault"), 
                      true
                     ));
        _operations[15] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetMediaResultsFromAllRuns_Deprecated_May10");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.tek.com/cerify/2009/12/02/reports/wsdl", "GetMediaResultsFromAllRuns"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.tek.com/cerify/2009/12/02/reports/wsdl", ">GetMediaResultsFromAllRuns"), com.tektronix.www.cerify.soap.client._20091202.GetMediaResultsFromAllRuns.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2009/12/02/reports/wsdl", ">GetMediaResultsFromAllRunsResponse"));
        oper.setReturnClass(com.tektronix.www.cerify.soap.client._20091202.GetMediaResultsFromAllRunsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.tek.com/cerify/2009/12/02/reports/wsdl", "GetMediaResultsFromAllRunsResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GeneralFault"),
                      "com.tektronix.www.cerify.soap.client.BaseCeritalkFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "BaseCeritalkFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetMediaFileResultsJobDoesntExistFault"),
                      "com.tektronix.www.cerify.soap.client.JobDoesntExistFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "JobDoesntExistFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetMediaFileResultsMediaFileNotInJobFault"),
                      "com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "MediaFileNotInJobFault"), 
                      true
                     ));
        _operations[16] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("AddMediaFileToMediaSet");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "AddMediaFileToMediaSet"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">AddMediaFileToMediaSet"), com.tektronix.www.cerify.soap.client.AddMediaFileToMediaSet.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">addMediaFileToMediaSetResponse"));
        oper.setReturnClass(com.tektronix.www.cerify.soap.client.AddMediaFileToMediaSetResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "addMediaFileToMediaSetResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "AddMediaFileToMediaSetURLNotInMediaLocationFault"),
                      "com.tektronix.www.cerify.soap.client.URLNotInMediaLocationFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "URLNotInMediaLocationFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "AddMediaFileToMediaSetMediaLocationDoesntExistFault"),
                      "com.tektronix.www.cerify.soap.client.MediaLocationDoesntExistFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "MediaLocationDoesntExistFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GeneralFault"),
                      "com.tektronix.www.cerify.soap.client.BaseCeritalkFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "BaseCeritalkFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "AddMediaFileToMediaSetMediaSetDoesntExistFault"),
                      "com.tektronix.www.cerify.soap.client.MediaSetDoesntExistFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "MediaSetDoesntExistFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "AddMediaFileToMediaSetURLNotAccessibleFault"),
                      "com.tektronix.www.cerify.soap.client.URLNotAccessibleFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "URLNotAccessibleFault"), 
                      true
                     ));
        _operations[17] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("ControlJob");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "ControlJob"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">ControlJob"), com.tektronix.www.cerify.soap.client.ControlJob.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">controlJobResponse"));
        oper.setReturnClass(com.tektronix.www.cerify.soap.client.ControlJobResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "controlJobResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GeneralFault"),
                      "com.tektronix.www.cerify.soap.client.BaseCeritalkFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "BaseCeritalkFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "ControlJobJobDoesntExistFault"),
                      "com.tektronix.www.cerify.soap.client.JobDoesntExistFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "JobDoesntExistFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "ControlJobJobIsArchivedFault"),
                      "com.tektronix.www.cerify.soap.client.JobIsArchivedFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "JobIsArchivedFault"), 
                      true
                     ));
        _operations[18] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("ControlMediaSet");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "ControlMediaSet"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">ControlMediaSet"), com.tektronix.www.cerify.soap.client.ControlMediaSet.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">controlMediaSetResponse"));
        oper.setReturnClass(com.tektronix.www.cerify.soap.client.ControlMediaSetResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "controlMediaSetResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "ControlMediaSetMediaSetIsInUseFault"),
                      "com.tektronix.www.cerify.soap.client.MediaSetIsInUseFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "MediaSetIsInUseFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GeneralFault"),
                      "com.tektronix.www.cerify.soap.client.BaseCeritalkFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "BaseCeritalkFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "ControlMediaSetMediaSetDoesntExistFault"),
                      "com.tektronix.www.cerify.soap.client.MediaSetDoesntExistFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "MediaSetDoesntExistFault"), 
                      true
                     ));
        _operations[19] = oper;

    }

    private static void _initOperationDesc3(){
        org.apache.axis.description.OperationDesc oper;
        org.apache.axis.description.ParameterDesc param;
        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetJobs");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetJobs"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetJobs"), com.tektronix.www.cerify.soap.client.GetJobs.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetJobsResponse"));
        oper.setReturnClass(com.tektronix.www.cerify.soap.client.GetJobsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetJobsResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GeneralFault"),
                      "com.tektronix.www.cerify.soap.client.BaseCeritalkFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "BaseCeritalkFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetJobsInvalidRangeFault"),
                      "com.tektronix.www.cerify.soap.client.InvalidRangeFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "InvalidRangeFault"), 
                      true
                     ));
        _operations[20] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetSystemStatus");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetSystemStatus"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetSystemStatus"), com.tektronix.www.cerify.soap.client.GetSystemStatus.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetSystemStatusResponse"));
        oper.setReturnClass(com.tektronix.www.cerify.soap.client.GetSystemStatusResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetSystemStatusResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GeneralFault"),
                      "com.tektronix.www.cerify.soap.client.BaseCeritalkFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "BaseCeritalkFault"), 
                      true
                     ));
        _operations[21] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetProfiles");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetProfiles"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetProfiles"), com.tektronix.www.cerify.soap.client.GetProfiles.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetProfilesResponse"));
        oper.setReturnClass(com.tektronix.www.cerify.soap.client.GetProfilesResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetProfilesResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GeneralFault"),
                      "com.tektronix.www.cerify.soap.client.BaseCeritalkFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "BaseCeritalkFault"), 
                      true
                     ));
        _operations[22] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetMediaLocations");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetMediaLocations"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetMediaLocations"), com.tektronix.www.cerify.soap.client.GetMediaLocations.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetMediaLocationsResponse"));
        oper.setReturnClass(com.tektronix.www.cerify.soap.client.GetMediaLocationsResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetMediaLocationsResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GeneralFault"),
                      "com.tektronix.www.cerify.soap.client.BaseCeritalkFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "BaseCeritalkFault"), 
                      true
                     ));
        _operations[23] = oper;

        oper = new org.apache.axis.description.OperationDesc();
        oper.setName("GetTemplatesForJob");
        param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetTemplatesForJob"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetTemplatesForJob"), com.tektronix.www.cerify.soap.client.GetTemplatesForJob.class, false, false);
        oper.addParameter(param);
        oper.setReturnType(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetTemplatesForJobResponse"));
        oper.setReturnClass(com.tektronix.www.cerify.soap.client.GetTemplatesForJobResponse.class);
        oper.setReturnQName(new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetTemplatesForJobResponse"));
        oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
        oper.setUse(org.apache.axis.constants.Use.LITERAL);
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GeneralFault"),
                      "com.tektronix.www.cerify.soap.client.BaseCeritalkFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "BaseCeritalkFault"), 
                      true
                     ));
        oper.addFault(new org.apache.axis.description.FaultDesc(
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "GetTemplatesForJobJobDoesntExistFault"),
                      "com.tektronix.www.cerify.soap.client.JobDoesntExistFault",
                      new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "JobDoesntExistFault"), 
                      true
                     ));
        _operations[24] = oper;

    }

    public CeriTalkSOAPStub() throws org.apache.axis.AxisFault {
         this(null);
    }

    public CeriTalkSOAPStub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         this(service);
         super.cachedEndpoint = endpointURL;
    }

    public CeriTalkSOAPStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
        if (service == null) {
            super.service = new org.apache.axis.client.Service();
        } else {
            super.service = service;
        }
        ((org.apache.axis.client.Service)super.service).setTypeMappingVersion("1.2");
            java.lang.Class cls;
            javax.xml.namespace.QName qName;
            javax.xml.namespace.QName qName2;
            java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
            java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
            java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
            java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
            java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
            java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
            java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
            java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
            java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
            java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">>>>>GetJobResultsResponse>job>jobmediafile>streaminfo>attribute");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.GetJobResultsResponseJobJobmediafileStreaminfoAttribute.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">>>>GetJobResultsResponse>job>jobmediafile>alert");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.GetJobResultsResponseJobJobmediafileAlert.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">>>>GetJobResultsResponse>job>jobmediafile>streaminfo");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.GetJobResultsResponseJobJobmediafileStreaminfo.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">>>GetJobResultsResponse>job>jobmediafile");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.GetJobResultsResponseJobJobmediafile.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">>>GetMediaFileResultsResponse>streaminfo>attribute");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponseStreaminfoAttribute.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">>GetJobResultsResponse>job");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.GetJobResultsResponseJob.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">>GetMediaFileResultsResponse>alert");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponseAlert.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">>GetMediaFileResultsResponse>streaminfo");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponseStreaminfo.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">>GetMediaLocationsResponse>medialocation");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.GetMediaLocationsResponseMedialocation.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">AddMediaFileToMediaSet");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.AddMediaFileToMediaSet.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">addMediaFileToMediaSetResponse");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.AddMediaFileToMediaSetResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">ControlJob");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.ControlJob.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">controlJobResponse");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.ControlJobResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">ControlMediaSet");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.ControlMediaSet.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">controlMediaSetResponse");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.ControlMediaSetResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">CreateJob");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.CreateJob.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">createJobResponse");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.CreateJobResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">CreateMediaSet");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.CreateMediaSet.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">createMediaSetResponse");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.CreateMediaSetResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetJobResults");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.GetJobResults.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetJobResultsResponse");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.GetJobResultsResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetJobs");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.GetJobs.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetJobsResponse");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.GetJobsResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetJobStatus");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.GetJobStatus.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetJobStatusResponse");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.GetJobStatusResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetMediaFileResults");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.GetMediaFileResults.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetMediaFileResultsResponse");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetMediaFileStatus");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.GetMediaFileStatus.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetMediaFileStatusResponse");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.GetMediaFileStatusResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetMediaLocations");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.GetMediaLocations.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetMediaLocationsResponse");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.GetMediaLocationsResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetProfiles");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.GetProfiles.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetProfilesResponse");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.GetProfilesResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetSystemStatus");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.GetSystemStatus.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetSystemStatusResponse");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.GetSystemStatusResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetTemplatesForJob");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.GetTemplatesForJob.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", ">GetTemplatesForJobResponse");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.GetTemplatesForJobResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "BaseCeritalkFault");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.BaseCeritalkFault.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "InvalidRangeFault");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.InvalidRangeFault.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "JobDoesntExistFault");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.JobDoesntExistFault.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "JobIsArchivedFault");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.JobIsArchivedFault.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "JobNameInUseFault");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.JobNameInUseFault.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "MediaFileNotInJobFault");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "MediaLocationDoesntExistFault");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.MediaLocationDoesntExistFault.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "MediaSetDoesntExistFault");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.MediaSetDoesntExistFault.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "MediaSetIsInUseFault");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.MediaSetIsInUseFault.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "MediaSetNameInUseFault");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.MediaSetNameInUseFault.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "ProfileDoesntExistFault");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.ProfileDoesntExistFault.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "URLNotAccessibleFault");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.URLNotAccessibleFault.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports/wsdl", "URLNotInMediaLocationFault");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.URLNotInMediaLocationFault.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports", "alertLevel");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.AlertLevel.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports", "alertType");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.AlertType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports", "cerifyUrl");
            cachedSerQNames.add(qName);
            cls = org.apache.axis.types.URI.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(org.apache.axis.encoding.ser.BaseSerializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleSerializerFactory.class, cls, qName));
            cachedDeserFactories.add(org.apache.axis.encoding.ser.BaseDeserializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleDeserializerFactory.class, cls, qName));

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports", "fileStatusType");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.FileStatusType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports", "jobActionType");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.JobActionType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports", "jobStatusType");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.JobStatusType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports", "mediaSetActionType");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.MediaSetActionType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports", "priorityType");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.PriorityType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports", "progress");
            cachedSerQNames.add(qName);
            cls = java.math.BigInteger.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(org.apache.axis.encoding.ser.BaseSerializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleSerializerFactory.class, cls, qName));
            cachedDeserFactories.add(org.apache.axis.encoding.ser.BaseDeserializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleDeserializerFactory.class, cls, qName));

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2007/03/22/reports", "resultType");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client.ResultType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2008/05/09/reports/wsdl", ">GetJobResults");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client._20080509.GetJobResults.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2008/05/09/reports/wsdl", ">GetMediaFileResults");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client._20080509.GetMediaFileResults.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2009/12/02/reports/wsdl", ">>>>GetMediaResultsFromAllRunsResponse>jobmediafile>streaminfo>attribute");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client._20091202.GetMediaResultsFromAllRunsResponseJobmediafileStreaminfoAttribute.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2009/12/02/reports/wsdl", ">>>GetMediaResultsFromAllRunsResponse>jobmediafile>alert");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client._20091202.GetMediaResultsFromAllRunsResponseJobmediafileAlert.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2009/12/02/reports/wsdl", ">>>GetMediaResultsFromAllRunsResponse>jobmediafile>streaminfo");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client._20091202.GetMediaResultsFromAllRunsResponseJobmediafileStreaminfo.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2009/12/02/reports/wsdl", ">>GetMediaResultsFromAllRunsResponse>jobmediafile");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client._20091202.GetMediaResultsFromAllRunsResponseJobmediafile.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2009/12/02/reports/wsdl", ">GetJobResults");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client._20091202.GetJobResults.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2009/12/02/reports/wsdl", ">GetMediaFileResults");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client._20091202.GetMediaFileResults.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2009/12/02/reports/wsdl", ">GetMediaResultsFromAllRuns");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client._20091202.GetMediaResultsFromAllRuns.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2009/12/02/reports/wsdl", ">GetMediaResultsFromAllRunsResponse");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client._20091202.GetMediaResultsFromAllRunsResponse.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2010/05/23/reports/wsdl", ">GetJobResults");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client._20100523.GetJobResults.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2010/05/23/reports/wsdl", ">GetMediaFileResults");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client._20100523.GetMediaFileResults.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2010/05/23/reports/wsdl", ">GetMediaResultsFromAllRuns");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client._20100523.GetMediaResultsFromAllRuns.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2010/12/20/reports/wsdl", ">GetJobResults");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client._20101220.GetJobResults.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2010/12/20/reports/wsdl", ">GetMediaFileResults");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client._20101220.GetMediaFileResults.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tek.com/cerify/2010/12/20/reports/wsdl", ">GetMediaResultsFromAllRuns");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.soap.client._20101220.GetMediaResultsFromAllRuns.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tektronix.com/cerify/templateInfo/2008/06/06", ">templateType>type");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.templateInfo._2008._06._06.TemplateTypeType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(enumsf);
            cachedDeserFactories.add(enumdf);

            qName = new javax.xml.namespace.QName("http://www.tektronix.com/cerify/templateInfo/2008/06/06", "templatesType");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.templateInfo._2008._06._06.TemplatesType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tektronix.com/cerify/templateInfo/2008/06/06", "templateType");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.templateInfo._2008._06._06.TemplateType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tektronix.com/cerify/templateInfo/2008/06/06", "testdefinitionrowinputType");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.templateInfo._2008._06._06.TestdefinitionrowinputType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tektronix.com/cerify/templateInfo/2008/06/06", "testdefinitionrowType");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.templateInfo._2008._06._06.TestdefinitionrowType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tektronix.com/cerify/templateInfo/2008/06/06", "testdefinitionType");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.templateInfo._2008._06._06.TestdefinitionType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

            qName = new javax.xml.namespace.QName("http://www.tektronix.com/cerify/templateInfo/2008/06/06", "versionType");
            cachedSerQNames.add(qName);
            cls = com.tektronix.www.cerify.templateInfo._2008._06._06.VersionType.class;
            cachedSerClasses.add(cls);
            cachedSerFactories.add(beansf);
            cachedDeserFactories.add(beandf);

    }

    protected org.apache.axis.client.Call createCall() throws java.rmi.RemoteException {
        try {
            org.apache.axis.client.Call _call = super._createCall();
            if (super.maintainSessionSet) {
                _call.setMaintainSession(super.maintainSession);
            }
            if (super.cachedUsername != null) {
                _call.setUsername(super.cachedUsername);
            }
            if (super.cachedPassword != null) {
                _call.setPassword(super.cachedPassword);
            }
            if (super.cachedEndpoint != null) {
                _call.setTargetEndpointAddress(super.cachedEndpoint);
            }
            if (super.cachedTimeout != null) {
                _call.setTimeout(super.cachedTimeout);
            }
            if (super.cachedPortName != null) {
                _call.setPortName(super.cachedPortName);
            }
            java.util.Enumeration keys = super.cachedProperties.keys();
            while (keys.hasMoreElements()) {
                java.lang.String key = (java.lang.String) keys.nextElement();
                _call.setProperty(key, super.cachedProperties.get(key));
            }
            // All the type mapping information is registered
            // when the first call is made.
            // The type mapping information is actually registered in
            // the TypeMappingRegistry of the service, which
            // is the reason why registration is only needed for the first call.
            synchronized (this) {
                if (firstCall()) {
                    // must set encoding style before registering serializers
                    _call.setEncodingStyle(null);
                    for (int i = 0; i < cachedSerFactories.size(); ++i) {
                        java.lang.Class cls = (java.lang.Class) cachedSerClasses.get(i);
                        javax.xml.namespace.QName qName =
                                (javax.xml.namespace.QName) cachedSerQNames.get(i);
                        java.lang.Object x = cachedSerFactories.get(i);
                        if (x instanceof Class) {
                            java.lang.Class sf = (java.lang.Class)
                                 cachedSerFactories.get(i);
                            java.lang.Class df = (java.lang.Class)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                        else if (x instanceof javax.xml.rpc.encoding.SerializerFactory) {
                            org.apache.axis.encoding.SerializerFactory sf = (org.apache.axis.encoding.SerializerFactory)
                                 cachedSerFactories.get(i);
                            org.apache.axis.encoding.DeserializerFactory df = (org.apache.axis.encoding.DeserializerFactory)
                                 cachedDeserFactories.get(i);
                            _call.registerTypeMapping(cls, qName, sf, df, false);
                        }
                    }
                }
            }
            return _call;
        }
        catch (java.lang.Throwable _t) {
            throw new org.apache.axis.AxisFault("Failure trying to get the Call object", _t);
        }
    }

    public com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponse getMediaFileResults(com.tektronix.www.cerify.soap.client._20101220.GetMediaFileResults request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.JobDoesntExistFault, com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[0]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "GetMediaFileResults"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {request});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponse) org.apache.axis.utils.JavaUtils.convert(_resp, com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.BaseCeritalkFault) {
              throw (com.tektronix.www.cerify.soap.client.BaseCeritalkFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.JobDoesntExistFault) {
              throw (com.tektronix.www.cerify.soap.client.JobDoesntExistFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault) {
              throw (com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponse getMediaFileResults_Deprecated(com.tektronix.www.cerify.soap.client.GetMediaFileResults request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.JobDoesntExistFault, com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[1]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "GetMediaFileResults_Deprecated"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {request});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponse) org.apache.axis.utils.JavaUtils.convert(_resp, com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.BaseCeritalkFault) {
              throw (com.tektronix.www.cerify.soap.client.BaseCeritalkFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.JobDoesntExistFault) {
              throw (com.tektronix.www.cerify.soap.client.JobDoesntExistFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault) {
              throw (com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponse getMediaFileResults_Deprecated_Dec09(com.tektronix.www.cerify.soap.client._20080509.GetMediaFileResults request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.JobDoesntExistFault, com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[2]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "GetMediaFileResults_Deprecated_Dec09"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {request});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponse) org.apache.axis.utils.JavaUtils.convert(_resp, com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.BaseCeritalkFault) {
              throw (com.tektronix.www.cerify.soap.client.BaseCeritalkFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.JobDoesntExistFault) {
              throw (com.tektronix.www.cerify.soap.client.JobDoesntExistFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault) {
              throw (com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponse getMediaFileResults_Deprecated_May10(com.tektronix.www.cerify.soap.client._20091202.GetMediaFileResults request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.JobDoesntExistFault, com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[3]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "GetMediaFileResults_Deprecated_May10"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {request});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponse) org.apache.axis.utils.JavaUtils.convert(_resp, com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.BaseCeritalkFault) {
              throw (com.tektronix.www.cerify.soap.client.BaseCeritalkFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.JobDoesntExistFault) {
              throw (com.tektronix.www.cerify.soap.client.JobDoesntExistFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault) {
              throw (com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponse getMediaFileResults_Deprecated_Dec10(com.tektronix.www.cerify.soap.client._20100523.GetMediaFileResults request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.JobDoesntExistFault, com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[4]);
        _call.setUseSOAPAction(true);
        _call.setSOAPActionURI("");
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "GetMediaFileResults_Deprecated_Dec10"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {request});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponse) org.apache.axis.utils.JavaUtils.convert(_resp, com.tektronix.www.cerify.soap.client.GetMediaFileResultsResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.BaseCeritalkFault) {
              throw (com.tektronix.www.cerify.soap.client.BaseCeritalkFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.JobDoesntExistFault) {
              throw (com.tektronix.www.cerify.soap.client.JobDoesntExistFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault) {
              throw (com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.tektronix.www.cerify.soap.client.CreateMediaSetResponse createMediaSet(com.tektronix.www.cerify.soap.client.CreateMediaSet request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.URLNotInMediaLocationFault, com.tektronix.www.cerify.soap.client.MediaLocationDoesntExistFault, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.MediaSetNameInUseFault, com.tektronix.www.cerify.soap.client.URLNotAccessibleFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[5]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "CreateMediaSet"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {request});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.tektronix.www.cerify.soap.client.CreateMediaSetResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.tektronix.www.cerify.soap.client.CreateMediaSetResponse) org.apache.axis.utils.JavaUtils.convert(_resp, com.tektronix.www.cerify.soap.client.CreateMediaSetResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.URLNotInMediaLocationFault) {
              throw (com.tektronix.www.cerify.soap.client.URLNotInMediaLocationFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.MediaLocationDoesntExistFault) {
              throw (com.tektronix.www.cerify.soap.client.MediaLocationDoesntExistFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.BaseCeritalkFault) {
              throw (com.tektronix.www.cerify.soap.client.BaseCeritalkFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.MediaSetNameInUseFault) {
              throw (com.tektronix.www.cerify.soap.client.MediaSetNameInUseFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.URLNotAccessibleFault) {
              throw (com.tektronix.www.cerify.soap.client.URLNotAccessibleFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.tektronix.www.cerify.soap.client.CreateJobResponse createJob(com.tektronix.www.cerify.soap.client.CreateJob request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.JobNameInUseFault, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.MediaSetDoesntExistFault, com.tektronix.www.cerify.soap.client.ProfileDoesntExistFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[6]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "CreateJob"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {request});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.tektronix.www.cerify.soap.client.CreateJobResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.tektronix.www.cerify.soap.client.CreateJobResponse) org.apache.axis.utils.JavaUtils.convert(_resp, com.tektronix.www.cerify.soap.client.CreateJobResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.JobNameInUseFault) {
              throw (com.tektronix.www.cerify.soap.client.JobNameInUseFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.BaseCeritalkFault) {
              throw (com.tektronix.www.cerify.soap.client.BaseCeritalkFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.MediaSetDoesntExistFault) {
              throw (com.tektronix.www.cerify.soap.client.MediaSetDoesntExistFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.ProfileDoesntExistFault) {
              throw (com.tektronix.www.cerify.soap.client.ProfileDoesntExistFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.tektronix.www.cerify.soap.client.GetMediaFileStatusResponse getMediaFileStatus(com.tektronix.www.cerify.soap.client.GetMediaFileStatus request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.JobDoesntExistFault, com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[7]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "GetMediaFileStatus"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {request});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.tektronix.www.cerify.soap.client.GetMediaFileStatusResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.tektronix.www.cerify.soap.client.GetMediaFileStatusResponse) org.apache.axis.utils.JavaUtils.convert(_resp, com.tektronix.www.cerify.soap.client.GetMediaFileStatusResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.BaseCeritalkFault) {
              throw (com.tektronix.www.cerify.soap.client.BaseCeritalkFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.JobDoesntExistFault) {
              throw (com.tektronix.www.cerify.soap.client.JobDoesntExistFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault) {
              throw (com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.tektronix.www.cerify.soap.client.GetJobStatusResponse getJobStatus(com.tektronix.www.cerify.soap.client.GetJobStatus request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.JobDoesntExistFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[8]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "GetJobStatus"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {request});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.tektronix.www.cerify.soap.client.GetJobStatusResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.tektronix.www.cerify.soap.client.GetJobStatusResponse) org.apache.axis.utils.JavaUtils.convert(_resp, com.tektronix.www.cerify.soap.client.GetJobStatusResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.BaseCeritalkFault) {
              throw (com.tektronix.www.cerify.soap.client.BaseCeritalkFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.JobDoesntExistFault) {
              throw (com.tektronix.www.cerify.soap.client.JobDoesntExistFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.tektronix.www.cerify.soap.client.GetJobResultsResponse getJobResults(com.tektronix.www.cerify.soap.client._20101220.GetJobResults request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.JobDoesntExistFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[9]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "GetJobResults"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {request});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.tektronix.www.cerify.soap.client.GetJobResultsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.tektronix.www.cerify.soap.client.GetJobResultsResponse) org.apache.axis.utils.JavaUtils.convert(_resp, com.tektronix.www.cerify.soap.client.GetJobResultsResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.BaseCeritalkFault) {
              throw (com.tektronix.www.cerify.soap.client.BaseCeritalkFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.JobDoesntExistFault) {
              throw (com.tektronix.www.cerify.soap.client.JobDoesntExistFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.tektronix.www.cerify.soap.client.GetJobResultsResponse getJobResults_Deprecated(com.tektronix.www.cerify.soap.client.GetJobResults request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.JobDoesntExistFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[10]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "GetJobResults_Deprecated"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {request});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.tektronix.www.cerify.soap.client.GetJobResultsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.tektronix.www.cerify.soap.client.GetJobResultsResponse) org.apache.axis.utils.JavaUtils.convert(_resp, com.tektronix.www.cerify.soap.client.GetJobResultsResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.BaseCeritalkFault) {
              throw (com.tektronix.www.cerify.soap.client.BaseCeritalkFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.JobDoesntExistFault) {
              throw (com.tektronix.www.cerify.soap.client.JobDoesntExistFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.tektronix.www.cerify.soap.client.GetJobResultsResponse getJobResults_Deprecated_Dec09(com.tektronix.www.cerify.soap.client._20080509.GetJobResults request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.JobDoesntExistFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[11]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "GetJobResults_Deprecated_Dec09"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {request});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.tektronix.www.cerify.soap.client.GetJobResultsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.tektronix.www.cerify.soap.client.GetJobResultsResponse) org.apache.axis.utils.JavaUtils.convert(_resp, com.tektronix.www.cerify.soap.client.GetJobResultsResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.BaseCeritalkFault) {
              throw (com.tektronix.www.cerify.soap.client.BaseCeritalkFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.JobDoesntExistFault) {
              throw (com.tektronix.www.cerify.soap.client.JobDoesntExistFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.tektronix.www.cerify.soap.client.GetJobResultsResponse getJobResults_Deprecated_May10(com.tektronix.www.cerify.soap.client._20091202.GetJobResults request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.JobDoesntExistFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[12]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "GetJobResults_Deprecated_May10"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {request});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.tektronix.www.cerify.soap.client.GetJobResultsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.tektronix.www.cerify.soap.client.GetJobResultsResponse) org.apache.axis.utils.JavaUtils.convert(_resp, com.tektronix.www.cerify.soap.client.GetJobResultsResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.BaseCeritalkFault) {
              throw (com.tektronix.www.cerify.soap.client.BaseCeritalkFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.JobDoesntExistFault) {
              throw (com.tektronix.www.cerify.soap.client.JobDoesntExistFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.tektronix.www.cerify.soap.client.GetJobResultsResponse getJobResults_Deprecated_Dec10(com.tektronix.www.cerify.soap.client._20100523.GetJobResults request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.JobDoesntExistFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[13]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "GetJobResults_Deprecated_Dec10"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {request});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.tektronix.www.cerify.soap.client.GetJobResultsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.tektronix.www.cerify.soap.client.GetJobResultsResponse) org.apache.axis.utils.JavaUtils.convert(_resp, com.tektronix.www.cerify.soap.client.GetJobResultsResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.BaseCeritalkFault) {
              throw (com.tektronix.www.cerify.soap.client.BaseCeritalkFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.JobDoesntExistFault) {
              throw (com.tektronix.www.cerify.soap.client.JobDoesntExistFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.tektronix.www.cerify.soap.client._20091202.GetMediaResultsFromAllRunsResponse getMediaResultsFromAllRuns(com.tektronix.www.cerify.soap.client._20101220.GetMediaResultsFromAllRuns request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.JobDoesntExistFault, com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[14]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "GetMediaResultsFromAllRuns"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {request});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.tektronix.www.cerify.soap.client._20091202.GetMediaResultsFromAllRunsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.tektronix.www.cerify.soap.client._20091202.GetMediaResultsFromAllRunsResponse) org.apache.axis.utils.JavaUtils.convert(_resp, com.tektronix.www.cerify.soap.client._20091202.GetMediaResultsFromAllRunsResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.BaseCeritalkFault) {
              throw (com.tektronix.www.cerify.soap.client.BaseCeritalkFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.JobDoesntExistFault) {
              throw (com.tektronix.www.cerify.soap.client.JobDoesntExistFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault) {
              throw (com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.tektronix.www.cerify.soap.client._20091202.GetMediaResultsFromAllRunsResponse getMediaResultsFromAllRuns_Deprecated_Dec10(com.tektronix.www.cerify.soap.client._20100523.GetMediaResultsFromAllRuns request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.JobDoesntExistFault, com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[15]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "GetMediaResultsFromAllRuns_Deprecated_Dec10"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {request});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.tektronix.www.cerify.soap.client._20091202.GetMediaResultsFromAllRunsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.tektronix.www.cerify.soap.client._20091202.GetMediaResultsFromAllRunsResponse) org.apache.axis.utils.JavaUtils.convert(_resp, com.tektronix.www.cerify.soap.client._20091202.GetMediaResultsFromAllRunsResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.BaseCeritalkFault) {
              throw (com.tektronix.www.cerify.soap.client.BaseCeritalkFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.JobDoesntExistFault) {
              throw (com.tektronix.www.cerify.soap.client.JobDoesntExistFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault) {
              throw (com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.tektronix.www.cerify.soap.client._20091202.GetMediaResultsFromAllRunsResponse getMediaResultsFromAllRuns_Deprecated_May10(com.tektronix.www.cerify.soap.client._20091202.GetMediaResultsFromAllRuns request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.JobDoesntExistFault, com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[16]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "GetMediaResultsFromAllRuns_Deprecated_May10"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {request});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.tektronix.www.cerify.soap.client._20091202.GetMediaResultsFromAllRunsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.tektronix.www.cerify.soap.client._20091202.GetMediaResultsFromAllRunsResponse) org.apache.axis.utils.JavaUtils.convert(_resp, com.tektronix.www.cerify.soap.client._20091202.GetMediaResultsFromAllRunsResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.BaseCeritalkFault) {
              throw (com.tektronix.www.cerify.soap.client.BaseCeritalkFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.JobDoesntExistFault) {
              throw (com.tektronix.www.cerify.soap.client.JobDoesntExistFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault) {
              throw (com.tektronix.www.cerify.soap.client.MediaFileNotInJobFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.tektronix.www.cerify.soap.client.AddMediaFileToMediaSetResponse addMediaFileToMediaSet(com.tektronix.www.cerify.soap.client.AddMediaFileToMediaSet request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.URLNotInMediaLocationFault, com.tektronix.www.cerify.soap.client.MediaLocationDoesntExistFault, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.MediaSetDoesntExistFault, com.tektronix.www.cerify.soap.client.URLNotAccessibleFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[17]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "AddMediaFileToMediaSet"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {request});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.tektronix.www.cerify.soap.client.AddMediaFileToMediaSetResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.tektronix.www.cerify.soap.client.AddMediaFileToMediaSetResponse) org.apache.axis.utils.JavaUtils.convert(_resp, com.tektronix.www.cerify.soap.client.AddMediaFileToMediaSetResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.URLNotInMediaLocationFault) {
              throw (com.tektronix.www.cerify.soap.client.URLNotInMediaLocationFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.MediaLocationDoesntExistFault) {
              throw (com.tektronix.www.cerify.soap.client.MediaLocationDoesntExistFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.BaseCeritalkFault) {
              throw (com.tektronix.www.cerify.soap.client.BaseCeritalkFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.MediaSetDoesntExistFault) {
              throw (com.tektronix.www.cerify.soap.client.MediaSetDoesntExistFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.URLNotAccessibleFault) {
              throw (com.tektronix.www.cerify.soap.client.URLNotAccessibleFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.tektronix.www.cerify.soap.client.ControlJobResponse controlJob(com.tektronix.www.cerify.soap.client.ControlJob request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.JobDoesntExistFault, com.tektronix.www.cerify.soap.client.JobIsArchivedFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[18]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "ControlJob"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {request});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.tektronix.www.cerify.soap.client.ControlJobResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.tektronix.www.cerify.soap.client.ControlJobResponse) org.apache.axis.utils.JavaUtils.convert(_resp, com.tektronix.www.cerify.soap.client.ControlJobResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.BaseCeritalkFault) {
              throw (com.tektronix.www.cerify.soap.client.BaseCeritalkFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.JobDoesntExistFault) {
              throw (com.tektronix.www.cerify.soap.client.JobDoesntExistFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.JobIsArchivedFault) {
              throw (com.tektronix.www.cerify.soap.client.JobIsArchivedFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.tektronix.www.cerify.soap.client.ControlMediaSetResponse controlMediaSet(com.tektronix.www.cerify.soap.client.ControlMediaSet request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.MediaSetIsInUseFault, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.MediaSetDoesntExistFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[19]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "ControlMediaSet"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {request});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.tektronix.www.cerify.soap.client.ControlMediaSetResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.tektronix.www.cerify.soap.client.ControlMediaSetResponse) org.apache.axis.utils.JavaUtils.convert(_resp, com.tektronix.www.cerify.soap.client.ControlMediaSetResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.MediaSetIsInUseFault) {
              throw (com.tektronix.www.cerify.soap.client.MediaSetIsInUseFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.BaseCeritalkFault) {
              throw (com.tektronix.www.cerify.soap.client.BaseCeritalkFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.MediaSetDoesntExistFault) {
              throw (com.tektronix.www.cerify.soap.client.MediaSetDoesntExistFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.tektronix.www.cerify.soap.client.GetJobsResponse getJobs(com.tektronix.www.cerify.soap.client.GetJobs request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.InvalidRangeFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[20]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "GetJobs"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {request});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.tektronix.www.cerify.soap.client.GetJobsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.tektronix.www.cerify.soap.client.GetJobsResponse) org.apache.axis.utils.JavaUtils.convert(_resp, com.tektronix.www.cerify.soap.client.GetJobsResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.BaseCeritalkFault) {
              throw (com.tektronix.www.cerify.soap.client.BaseCeritalkFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.InvalidRangeFault) {
              throw (com.tektronix.www.cerify.soap.client.InvalidRangeFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.tektronix.www.cerify.soap.client.GetSystemStatusResponse getSystemStatus(com.tektronix.www.cerify.soap.client.GetSystemStatus request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[21]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "GetSystemStatus"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {request});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.tektronix.www.cerify.soap.client.GetSystemStatusResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.tektronix.www.cerify.soap.client.GetSystemStatusResponse) org.apache.axis.utils.JavaUtils.convert(_resp, com.tektronix.www.cerify.soap.client.GetSystemStatusResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.BaseCeritalkFault) {
              throw (com.tektronix.www.cerify.soap.client.BaseCeritalkFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.tektronix.www.cerify.soap.client.GetProfilesResponse getProfiles(com.tektronix.www.cerify.soap.client.GetProfiles request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[22]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "GetProfiles"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {request});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.tektronix.www.cerify.soap.client.GetProfilesResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.tektronix.www.cerify.soap.client.GetProfilesResponse) org.apache.axis.utils.JavaUtils.convert(_resp, com.tektronix.www.cerify.soap.client.GetProfilesResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.BaseCeritalkFault) {
              throw (com.tektronix.www.cerify.soap.client.BaseCeritalkFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.tektronix.www.cerify.soap.client.GetMediaLocationsResponse getMediaLocations(com.tektronix.www.cerify.soap.client.GetMediaLocations request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[23]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "GetMediaLocations"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {request});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.tektronix.www.cerify.soap.client.GetMediaLocationsResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.tektronix.www.cerify.soap.client.GetMediaLocationsResponse) org.apache.axis.utils.JavaUtils.convert(_resp, com.tektronix.www.cerify.soap.client.GetMediaLocationsResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.BaseCeritalkFault) {
              throw (com.tektronix.www.cerify.soap.client.BaseCeritalkFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

    public com.tektronix.www.cerify.soap.client.GetTemplatesForJobResponse getTemplatesForJob(com.tektronix.www.cerify.soap.client.GetTemplatesForJob request) throws java.rmi.RemoteException, com.tektronix.www.cerify.soap.client.BaseCeritalkFault, com.tektronix.www.cerify.soap.client.JobDoesntExistFault {
        if (super.cachedEndpoint == null) {
            throw new org.apache.axis.NoEndPointException();
        }
        org.apache.axis.client.Call _call = createCall();
        _call.setOperation(_operations[24]);
        _call.setEncodingStyle(null);
        _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
        _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
        _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
        _call.setOperationName(new javax.xml.namespace.QName("", "GetTemplatesForJob"));

        setRequestHeaders(_call);
        setAttachments(_call);
 try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {request});

        if (_resp instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)_resp;
        }
        else {
            extractAttachments(_call);
            try {
                return (com.tektronix.www.cerify.soap.client.GetTemplatesForJobResponse) _resp;
            } catch (java.lang.Exception _exception) {
                return (com.tektronix.www.cerify.soap.client.GetTemplatesForJobResponse) org.apache.axis.utils.JavaUtils.convert(_resp, com.tektronix.www.cerify.soap.client.GetTemplatesForJobResponse.class);
            }
        }
  } catch (org.apache.axis.AxisFault axisFaultException) {
    if (axisFaultException.detail != null) {
        if (axisFaultException.detail instanceof java.rmi.RemoteException) {
              throw (java.rmi.RemoteException) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.BaseCeritalkFault) {
              throw (com.tektronix.www.cerify.soap.client.BaseCeritalkFault) axisFaultException.detail;
         }
        if (axisFaultException.detail instanceof com.tektronix.www.cerify.soap.client.JobDoesntExistFault) {
              throw (com.tektronix.www.cerify.soap.client.JobDoesntExistFault) axisFaultException.detail;
         }
   }
  throw axisFaultException;
}
    }

}
