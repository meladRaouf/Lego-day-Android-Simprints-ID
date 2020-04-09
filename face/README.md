# Face modality

The face modality was created as a way to have contactless biometrics as an option for SimprintsID.

##  Important links
[Confluence](https://simprints.atlassian.net/wiki/spaces/CS/overview)  
[Jira](https://simprints.atlassian.net/secure/RapidBoard.jspa?rapidView=19)

## Flow overview

SimprintsID start the face modality by creating an `Intent` to [FaceOrchestratorActivity.kt](src/main/java/com/simprints/face/orchestrator/FaceOrchestratorActivity.kt). In this `Intent` an [IFaceRequest.kt](../moduleapi/src/main/java/com/simprints/moduleapi/face/requests/IFaceRequest.kt) is mandatory. When getting the request from SimprintsID, `FaceOrchestratorViewModel` needs to transform it from a ModuleAPI interface to a domain class.

After the flow is finished (images are captured or matched against a probe), `FaceOrchestratorViewModel` transforms the domain response to a ModuleAPI interface and returns the result to SimprintsID as an `Intent` inside the activity result.

To understand more how the capture flow works, check the capture [README](src/main/java/com/simprints/face/capture/README.md).

## Detection

The detection phase is when the app analyzes a PreviewFrame (already cropped by FrameProcessor) and returns a Face that it found (or null if none). The methods on the Detectors are suspend functions because the process to get a Face can be onerous (you should use `withContext(Dispatchers.IO)`). Note that the Face returned also have a template already in it, making it a one step to find a face and extract the template for the app. It was done that way because most SDKs tested returned a template when looking for a face. Currently we only use the analyze method that receives a `PreviewFrame`, the method to analyze a `Bitmap` is there for future necessity (it was used by the R&D team).

### MockFaceDetector

This is a mock detector that always return true and wait a bit (200ms) before returning a face with an empty template.