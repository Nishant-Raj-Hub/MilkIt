const mongoose = require("mongoose")

const milkRecordSchema = new mongoose.Schema(
  {
    userId: {
      type: mongoose.Schema.Types.ObjectId,
      ref: "User",
      required: true,
      index: true,
    },
    date: {
      type: Date,
      required: true,
      index: true,
    },
    liters: {
      type: Number,
      required: true,
      min: 0,
      max: 50, // reasonable limit
      default: 1,
    },
    status: {
      type: String,
      enum: ["received", "not_received", "partial"],
      default: "received",
    },
    isAutoMarked: {
      type: Boolean,
      default: true,
    },
    notes: {
      type: String,
      maxlength: 200,
      trim: true,
    },
    milkType: {
      type: String,
      enum: ["cow", "buffalo", "packet", "other"],
      default: "cow",
    },
    createdAt: {
      type: Date,
      default: Date.now,
    },
    updatedAt: {
      type: Date,
      default: Date.now,
    },
  },
  {
    timestamps: true,
  },
)

// Compound index for efficient queries
milkRecordSchema.index({ userId: 1, date: 1 }, { unique: true })

// Update the updatedAt field on save
milkRecordSchema.pre("save", function (next) {
  this.updatedAt = new Date()
  next()
})

module.exports = mongoose.model("MilkRecord", milkRecordSchema)
