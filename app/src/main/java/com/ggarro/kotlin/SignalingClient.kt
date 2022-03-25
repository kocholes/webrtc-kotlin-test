package com.ggarro.kotlin

import com.google.firebase.firestore.*
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

class SignalingClient {
    private val db = FirebaseFirestore.getInstance()
    private var call: DocumentReference? = null
    private var sessionType: SessionDescription.Type? = null

    constructor() {
        call = db.collection("calls").document()
        sessionType = SessionDescription.Type.OFFER
    }

    constructor(connectionId: String) {
        call = db.collection("calls").document(connectionId)
        sessionType = SessionDescription.Type.ANSWER
    }

    fun getConnectionId(): String? {
        return call?.id
    }

    fun getOfferCandidates(): CollectionReference? {
        return call?.collection("offerCandidates")
    }

    fun getAnswerCandidates(): CollectionReference? {
        return call?.collection("answerCandidates")
    }

    fun addLocalIceCandidate(iceCandidate: IceCandidate) {
        val data = iceCandidateToDbData(iceCandidate)

        val candidates =
            if (sessionType == SessionDescription.Type.OFFER) getOfferCandidates()
            else getAnswerCandidates()
        candidates?.add(data)
    }

    fun setLocalDescription(sessionDescription: SessionDescription) {
        val data = sessionDescriptionToDbData(sessionDescription)
        call?.set(data)
    }

    fun onRemoteDescription(callback: (SessionDescription) -> Unit) {
        call?.addSnapshotListener { snapshot, _ ->
            // I want the opposite type because the description is from the remote peer
            val typeKey = if (sessionType == SessionDescription.Type.OFFER) "answer" else "offer"
            val data = snapshot?.data
            if (data != null && data.containsKey(typeKey)) {
                val dbData = data[typeKey] as Map<String, Any>
                val sessionDescription = dbDataToSessionDescription(dbData)
                callback(sessionDescription)
            }
        }
    }

    fun onRemoteIceCandidate(callback: (IceCandidate) -> Unit) {
        // I want the opposite candidates because the iceServer is from the remote peer
        val candidates =
            if (sessionType == SessionDescription.Type.OFFER) getAnswerCandidates()
            else getOfferCandidates()
        candidates?.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                for (change in snapshot.documentChanges) {
                    if (change.type == DocumentChange.Type.ADDED) {
                        val data = change.document.data
                        val candidate = dbDataToIceCandidate(data)
                        callback(candidate)
                    }
                }
            }
        }
    }

    // Data Transformation

    private fun iceCandidateToDbData(iceCandidate: IceCandidate): Map<String, Any?> {
        return mapOf(
            "candidate" to iceCandidate.sdp,
            "sdpMLineIndex" to iceCandidate.sdpMLineIndex,
            "sdpMid" to iceCandidate.sdpMid
        )
    }

    private fun dbDataToIceCandidate(data: Map<String, Any>): IceCandidate {
        val sdpMid = data["sdpMid"].toString()
        val sdpMLineIndex = data["sdpMLineIndex"] as Long
        val sdp = data["candidate"].toString()
        return IceCandidate(sdpMid, sdpMLineIndex.toInt(), sdp)
    }

    private fun sessionDescriptionToDbData(sessionDescription: SessionDescription): Map<String, Any?> {
        val type = sessionDescription.type.toString().lowercase()
        return mapOf(
            type to mapOf(
                "type" to type,
                "sdp" to sessionDescription.description
            )
        )
    }

    private fun dbDataToSessionDescription(data: Map<String, Any>): SessionDescription {
        val type = SessionDescription.Type.valueOf(
            data["type"].toString().uppercase()
        )
        val sdp = data["sdp"].toString()
        return SessionDescription(type, sdp)
    }
}