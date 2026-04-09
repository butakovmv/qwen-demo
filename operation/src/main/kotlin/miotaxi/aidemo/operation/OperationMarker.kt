package miotaxi.aidemo.operation

import miotaxi.aidemo.operation.answer.AnswerMarker
import miotaxi.aidemo.operation.question.QuestionMarker

/** Marker interface for component scanning of the operation module. */
interface OperationMarker : QuestionMarker, AnswerMarker
