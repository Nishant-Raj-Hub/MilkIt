const mongoose = require("mongoose")
const bcrypt = require("bcryptjs")

const userSchema = new mongoose.Schema(
  {
    username: {
      type: String,
      required: true,
      unique: true,
      trim: true,
      minlength: 3,
      maxlength: 30,
    },
    phone: {
      type: String,
      required: true,
      unique: true,
      trim: true,
      match: /^[0-9]{10}$/,
    },
    passwordHash: {
      type: String,
      required: true,
      minlength: 6,
    },
    createdAt: {
      type: Date,
      default: Date.now,
    },
    lastLogin: {
      type: Date,
      default: Date.now,
    },
    isActive: {
      type: Boolean,
      default: true,
    },
  },
  {
    timestamps: true,
  },
)

// Hash password before saving
userSchema.pre("save", async function (next) {
  if (!this.isModified("passwordHash")) return next()

  try {
    const salt = await bcrypt.genSalt(12)
    this.passwordHash = await bcrypt.hash(this.passwordHash, salt)
    next()
  } catch (error) {
    next(error)
  }
})

// Compare password method
userSchema.methods.comparePassword = async function (candidatePassword) {
  return bcrypt.compare(candidatePassword, this.passwordHash)
}

// Remove sensitive data when converting to JSON
userSchema.methods.toJSON = function () {
  const userObject = this.toObject()
  delete userObject.passwordHash
  delete userObject.__v
  return userObject
}

module.exports = mongoose.model("User", userSchema)
