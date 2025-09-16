const express = require("express")
const User = require("../models/User")
const { generateToken, authenticateToken } = require("../middleware/auth")
const rateLimit = require("express-rate-limit")

const router = express.Router()

// Rate limiting for auth routes
const authLimiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 5, // limit each IP to 5 requests per windowMs for auth routes
  message: "Too many authentication attempts, please try again later.",
})

/**
 * @route POST /api/auth/signup
 * @desc Register a new user
 * @access Public
 */
router.post("/signup", authLimiter, async (req, res) => {
  try {
    const { username, phone, password } = req.body

    // Validation
    if (!username || !phone || !password) {
      return res.status(400).json({
        error: "All fields are required",
        details: "Username, phone, and password must be provided",
      })
    }

    if (password.length < 6) {
      return res.status(400).json({
        error: "Password must be at least 6 characters long",
      })
    }

    if (!/^[0-9]{10}$/.test(phone)) {
      return res.status(400).json({
        error: "Phone number must be exactly 10 digits",
      })
    }

    if (username.length < 3 || username.length > 30) {
      return res.status(400).json({
        error: "Username must be between 3 and 30 characters",
      })
    }

    // Check if user already exists
    const existingUser = await User.findOne({
      $or: [{ username }, { phone }],
    })

    if (existingUser) {
      return res.status(409).json({
        error: "User already exists",
        details: existingUser.username === username ? "Username taken" : "Phone number already registered",
      })
    }

    // Create new user
    const user = new User({
      username: username.toLowerCase().trim(),
      phone: phone.trim(),
      passwordHash: password, // Will be hashed by pre-save middleware
    })

    await user.save()

    // Generate token
    const token = generateToken(user._id)

    res.status(201).json({
      message: "User created successfully",
      user: {
        id: user._id,
        username: user.username,
        phone: user.phone,
        createdAt: user.createdAt,
      },
      token,
    })
  } catch (error) {
    console.error("Signup error:", error)

    if (error.code === 11000) {
      // Duplicate key error
      const field = Object.keys(error.keyPattern)[0]
      return res.status(409).json({
        error: `${field} already exists`,
      })
    }

    res.status(500).json({
      error: "Failed to create user",
      message: process.env.NODE_ENV === "development" ? error.message : "Internal server error",
    })
  }
})

/**
 * @route POST /api/auth/login
 * @desc Authenticate user and return token
 * @access Public
 */
router.post("/login", authLimiter, async (req, res) => {
  try {
    const { identifier, password } = req.body // identifier can be username or phone

    // Validation
    if (!identifier || !password) {
      return res.status(400).json({
        error: "Username/phone and password are required",
      })
    }

    // Find user by username or phone
    const user = await User.findOne({
      $or: [{ username: identifier.toLowerCase().trim() }, { phone: identifier.trim() }],
      isActive: true,
    })

    if (!user) {
      return res.status(401).json({
        error: "Invalid credentials",
        details: "User not found or account deactivated",
      })
    }

    // Check password
    const isPasswordValid = await user.comparePassword(password)
    if (!isPasswordValid) {
      return res.status(401).json({
        error: "Invalid credentials",
        details: "Incorrect password",
      })
    }

    // Update last login
    user.lastLogin = new Date()
    await user.save()

    // Generate token
    const token = generateToken(user._id)

    res.json({
      message: "Login successful",
      user: {
        id: user._id,
        username: user.username,
        phone: user.phone,
        lastLogin: user.lastLogin,
      },
      token,
    })
  } catch (error) {
    console.error("Login error:", error)
    res.status(500).json({
      error: "Login failed",
      message: process.env.NODE_ENV === "development" ? error.message : "Internal server error",
    })
  }
})

/**
 * @route GET /api/auth/profile
 * @desc Get current user profile
 * @access Private
 */
router.get("/profile", authenticateToken, async (req, res) => {
  try {
    res.json({
      user: {
        id: req.user._id,
        username: req.user.username,
        phone: req.user.phone,
        createdAt: req.user.createdAt,
        lastLogin: req.user.lastLogin,
      },
    })
  } catch (error) {
    console.error("Profile error:", error)
    res.status(500).json({
      error: "Failed to fetch profile",
    })
  }
})

/**
 * @route PUT /api/auth/profile
 * @desc Update user profile
 * @access Private
 */
router.put("/profile", authenticateToken, async (req, res) => {
  try {
    const { username, phone } = req.body
    const userId = req.user._id

    const updateData = {}

    if (username) {
      if (username.length < 3 || username.length > 30) {
        return res.status(400).json({
          error: "Username must be between 3 and 30 characters",
        })
      }
      updateData.username = username.toLowerCase().trim()
    }

    if (phone) {
      if (!/^[0-9]{10}$/.test(phone)) {
        return res.status(400).json({
          error: "Phone number must be exactly 10 digits",
        })
      }
      updateData.phone = phone.trim()
    }

    if (Object.keys(updateData).length === 0) {
      return res.status(400).json({
        error: "No valid fields to update",
      })
    }

    // Check for duplicates
    const existingUser = await User.findOne({
      _id: { $ne: userId },
      $or: [
        ...(updateData.username ? [{ username: updateData.username }] : []),
        ...(updateData.phone ? [{ phone: updateData.phone }] : []),
      ],
    })

    if (existingUser) {
      return res.status(409).json({
        error: "Username or phone already exists",
      })
    }

    const updatedUser = await User.findByIdAndUpdate(userId, updateData, {
      new: true,
      runValidators: true,
    })

    res.json({
      message: "Profile updated successfully",
      user: {
        id: updatedUser._id,
        username: updatedUser.username,
        phone: updatedUser.phone,
        createdAt: updatedUser.createdAt,
        lastLogin: updatedUser.lastLogin,
      },
    })
  } catch (error) {
    console.error("Profile update error:", error)

    if (error.code === 11000) {
      const field = Object.keys(error.keyPattern)[0]
      return res.status(409).json({
        error: `${field} already exists`,
      })
    }

    res.status(500).json({
      error: "Failed to update profile",
      message: process.env.NODE_ENV === "development" ? error.message : "Internal server error",
    })
  }
})

/**
 * @route POST /api/auth/change-password
 * @desc Change user password
 * @access Private
 */
router.post("/change-password", authenticateToken, async (req, res) => {
  try {
    const { currentPassword, newPassword } = req.body
    const userId = req.user._id

    if (!currentPassword || !newPassword) {
      return res.status(400).json({
        error: "Current password and new password are required",
      })
    }

    if (newPassword.length < 6) {
      return res.status(400).json({
        error: "New password must be at least 6 characters long",
      })
    }

    // Get user with password
    const user = await User.findById(userId)
    if (!user) {
      return res.status(404).json({
        error: "User not found",
      })
    }

    // Verify current password
    const isCurrentPasswordValid = await user.comparePassword(currentPassword)
    if (!isCurrentPasswordValid) {
      return res.status(401).json({
        error: "Current password is incorrect",
      })
    }

    // Update password
    user.passwordHash = newPassword // Will be hashed by pre-save middleware
    await user.save()

    res.json({
      message: "Password changed successfully",
    })
  } catch (error) {
    console.error("Change password error:", error)
    res.status(500).json({
      error: "Failed to change password",
      message: process.env.NODE_ENV === "development" ? error.message : "Internal server error",
    })
  }
})

/**
 * @route POST /api/auth/logout
 * @desc Logout user (client-side token removal)
 * @access Private
 */
router.post("/logout", authenticateToken, (req, res) => {
  // In a JWT-based system, logout is typically handled client-side
  // by removing the token from storage
  res.json({
    message: "Logout successful",
    note: "Please remove the token from client storage",
  })
})

module.exports = router
