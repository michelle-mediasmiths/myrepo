package com.mediasmiths.foxtel.ip.common.events;

public class EventNames
{
	public static final String ARDOME_IMPORT_FAILURE_NON_AO = "ArdomeImportFailureNonAO";
	public static final String ARDOME_IMPORT_FAILURE_AO = "ArdomeImportFailureAO";
	public static final String COMPLIANCE_LOGGING_MARKER = "ComplianceLoggingMarker";
	public static final String QC_FAILED_RE_ORDER = "QcFailedReOrder";
	public static final String CHANNEL_CONDITIONS_FOUND_DURING_QC = "ChannelConditionsFoundDuringQC";
	public static final String COMM_ERROR = "CommError";
	public static final String FILE_PICK_UP_NOTIFICATION = "FilePickUpNotification";
	public static final String AO_MEDIA_IMPORT_TO_ARDOME_FAILURE = "AOMediaImportToArdomeFailure";
	public static final String NON_AO_MEDIA_IMPORT_TO_ARDOME_FAILURE = "NonAOMediaImportToArdomeFailure";
	public static final String AO_CONTENT_WITHOUT_MATERIAL_ID = "AOContentWithoutMaterialID";
	public static final String NON_AO_CONTENT_WITHOUT_MATERIAL_ID = "NonAOContentWithoutMaterialID";
	public static final String QUARANTINE = "Quarantine";
	public static final String AO_PLACE_HOLDER_CANNOT_BE_IDENTIFIED = "AOPlaceHolderCannotBeIdentified";
	public static final String NON_AO_PLACE_HOLDER_CANNOT_BE_IDENTIFIED = "NonAOPlaceHolderCannotBeIdentified";
	public static final String AO_CONTENT_WITHOUT_COMPANION_XML = "AOContentWithoutCompanionXML";
	public static final String NON_AO_CONTENT_WITHOUT_COMPANION_XML = "NonAOContentWithoutCompanionXML";
	public static final String PLACEHOLDER_ALREADY_HAS_MEDIA = "PlaceholderAlreadyHasMedia";
	public final static String CHANNEL_CONDITIONS_FOUND_IN_RUZZ_XML = "ChannelConditionsFoundInRuzzXml";
	public static final String MANUAL_PURGE = "ManualPurge";
	public static final String EXPORT_START = "ExportStart";
	public static final String ADD_OR_UPDATE_MATERIAL = "AddOrUpdateMaterial";
	public static final String MANUAL_QA = "ManualQA";
	public static final String PREVIEW_FURTHER_ANALYSIS = "PreviewFurtherAnalysis";
	public static final String PREVIEW_FAILED = "PreviewFailed";
	public static final String PREVIEW_PASSED_REORDER = "PreviewPassedReorder";
	public static final String PREVIEW_FIX_REORDER = "PreviewFixReorder";
	public static final String AUTO_QC_REPORT = "AutoQCReport";
	public static final String FAILED_IN_SENDTO_TX = "FailedInSendtoTX";
	public static final String BMS_FAILURE = "BMSFailure";
	public static final String IBMS_ROLL_BACK_DELETE_MATERIAL_FAILS = "IBMSRollBackDeleteMaterialFails";
	public static final String IBMS_ROLL_BACK_DELETE_TITLE_FAILS = "IBMSRollBackDeleteTitleFails";
	public static final String PROTECTED_PURGE_FAIL = "ProtectedPurgeFail";
	public static final String CREATEOR_UPDATE_TITLE = "CreateorUpdateTitle";
	public static final String ADD_OR_UPDATE_PACKAGE = "AddOrUpdatePackage";
	public static final String DELETE_PACKAGE = "DeletePackage";
	public static final String DELETE_MATERIAL = "DeleteMaterial";
	public static final String PURGE_TITLE = "PurgeTitle";
	public static final String QC_SERVER_FAILURE_DURING_TRANSCODE = "QcServerFailureDuringTranscode";
	public static final String QC_SERVER_FAIL = "QcServerFail";
	public static final String AUTO_QC_PASSED = "AutoQCPassed";
	public static final String EXPORT_FAILURE = "ExportFailure";
	public static final String TRANSCODE_DELIVERY_FAILED = "TranscodeDeliveryFailed";
	public static final String TRANSCODE_DELIVERY_FAILED_FXP_TRANSFER = "TranscodeDeliveryFailedFXPTransfer";
	public static final String FAILED_TO_GENERATE_XML = "FailedToGenerateXML";
	public static final String FAILTED_TO_MOVE_TX_XML_TO_DELIVERY_LOCATION = "TxXmlDeliveryFailed";
	public static final String FAILTED_TO_MOVE_TX_XML_TO_FTP_LOCATION = "TxXmlFTPDeliveryFailed";
	public static final String CAPTION_PROXY_SUCCESS = "CaptionProxySuccess";
	public static final String CLASSIFICATION_PROXY_SUCCESS = "ClassificationProxySuccess";
	public static final String PUBLICITY_PROXY_SUCCESS = "PublicityProxySuccess";
	public static final String TC_FAILED = "TCFailed";
	public static final String CLASSIFICATION_PROXY_FAILURE = "ClassificationProxyFailure";
	public static final String PUBLICITY_PROXY_FAILURE = "PublicityProxyFailure";
	public static final String CAPTION_PROXY_FAILURE = "CaptionProxyFailure";
	public static final String QC_PROBLEM_WITH_TC_MEDIA = "QCProblemWithTCMedia";
	public static final String CERIFY_QC_ERROR = "CerifyQCError";
	public static final String MARKETING_CONTENT_AVAILABLE = "MarketingContentAvailable";
	public static final String PROGRAMME_CONTENT_AVAILABLE = "ProgrammeContentAvailable";
	public static final String PURGE_EVENT_NOTIFICATION = "PurgeEventNotification";
	public static final String EXTENDED_PUBLISHING_TASK_EVENT = "ExtendedPublishingTask";
	public static final String COMPLIANCE_LOGGING_TASK_EVENT = "ComplianceLoggingTaskEvent";
	public static final String DISK_USAGE_EVENT = "DiskUsageEvent";
	public static final String TRANSCODE_REPORT_DATA = "TranscodeReportData";
}
