const express = require("express")
const mongoose = require("mongoose")
const cors = require("cors")
const helmet = require("helmet")
const rateLimit = require("express-rate-limit")
const cron = require("node-cron")
require("dotenv").config()

const authRoutes = require("./routes/auth")
const milkRoutes = require("./routes/milk")
const exportRoutes = require("./routes/export")
const { cleanupOldRecords } = require("./utils/cleanup")

const app = express()
const PORT = process.env.PORT || 3000

// Security middleware
app.use(helmet())
app.use(
  cors({
    origin: process.env.NODE_ENV === "production" ? ["your-production-domain.com"] : true,
    credentials: true,
  }),
)

// Rate limiting
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100, // limit each IP to 100 requests per windowMs
  message: "Too many requests from this IP, please try again later.",
})
app.use(limiter)

// Body parsing middleware
app.use(express.json({ limit: "10mb" }))
app.use(express.urlencoded({ extended: true }))

// MongoDB connection
mongoose
  .connect(process.env.MONGO_URI, {
    useNewUrlParser: true,
    useUnifiedTopology: true,
    dbName: process.env.DB_NAME,
  })
  .then(() => {
    console.log("✅ Connected to MongoDB Atlas")
  })
  .catch((error) => {
    console.error("❌ MongoDB connection error:", error)
    process.exit(1)
  })

// Routes
app.use("/api/auth", authRoutes)
app.use("/api/milk", milkRoutes)
app.use("/api/export", exportRoutes)

// Health check endpoint
app.get("/api/health", (req, res) => {
  res.json({
    status: "OK",
    timestamp: new Date().toISOString(),
    uptime: process.uptime(),
  })
})

// Cleanup old records daily at 2 AM
cron.schedule("0 2 * * *", async () => {
  console.log("🧹 Running daily cleanup of old records...")
  try {
    await cleanupOldRecords()
    console.log("✅ Cleanup completed successfully")
  } catch (error) {
    console.error("❌ Cleanup failed:", error)
  }
})

// Error handling middleware
app.use((err, req, res, next) => {
  console.error(err.stack)
  res.status(500).json({
    error: "Something went wrong!",
    message: process.env.NODE_ENV === "development" ? err.message : "Internal server error",
  })
})

// 404 handler
app.use("*", (req, res) => {
  res.status(404).json({ error: "Route not found" })
})

app.listen(PORT, () => {
  console.log(`🚀 MilkIt Backend Server running on port ${PORT}`)
  console.log(`📊 Environment: ${process.env.NODE_ENV}`)
})

module.exports = app
